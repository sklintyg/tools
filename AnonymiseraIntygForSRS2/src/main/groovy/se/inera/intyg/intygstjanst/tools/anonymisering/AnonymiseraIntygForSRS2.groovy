/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.tools.anonymisering


import groovy.sql.Sql
import groovyx.gpars.GParsPool

import org.xml.sax.SAXException
import groovy.json.*
import javax.xml.parsers.ParserConfigurationException
import javax.xml.xpath.XPathExpressionException

import java.sql.Blob
import java.util.concurrent.atomic.AtomicInteger

import org.apache.commons.dbcp2.BasicDataSource

import se.inera.intyg.common.tools.anonymisering.AnonymiseraHsaId
import se.inera.intyg.common.tools.anonymisering.AnonymiseraConsistentPersonId
import se.inera.intyg.common.tools.anonymisering.AnonymiseraEndastPersonnummerXmlForSRS


class AnonymiseraIntygForSRS2 {
    static File file = new File("output.csv")
    static FileWriter fileWriter = new FileWriter(file)

    static final List<String> headers = [
            "lakartlatande-id", "typAvUtlatande", "signeringsdatum",
            "patient-id", "patient-ålder", "patient-kön",
            "personal-id", "enhets-id", "vardgivar-id",
            "vardkontakt-typ-1", "vardkontakt-tid-1", "vardkontakt-typ-2", "vardkontakt-tid-2",
            "referens-typ-1", "referens-tid-1", "referens-typ-2", "referens-tid-2",
            "aktivitet.avstangdPgaSmittskydd",
            "aktivitet.KontaktArbetsformedling",
            "aktivitet.kontaktForetagsVard",

            "aktivitet.Ovrigt",
            "aktivitet.Ovrigt.beskrivning",

            "aktivitet.planeradPagaendeAtgardSjukvard",
            "aktivitet.planeradPagaendeAtgardSjukvardBeskrivning",

            "aktivitet.planeradPagaendeAnnanAtgard",
            "aktivitet.planeradPagaendeAnnanAtgardBeskrivning",

            "aktivitet.arbetslivsinriktadRehabAktuell",
            "aktivitet.arbetslivsinriktadRehabEjAktuell",
            "aktivitet.garEjBedommaOmArbetslivsinriktadRehabAktuell",
            "aktivitet.forandrattRessattAktuellt",
            "aktivitet.forandrattRessattEjAktuellt",
            "aktivitet.kontaktMedForsakringskassanAktuellt",

            "medicinskttillstand.Diagnoskod",
            "medicinskttillstand.Beskrivning",

            "bedomttillstand.beskrivning",

            "funktionstillstand.kroppsfunktion",
            "funktionstillstand.kroppsfunktion.beskrivning",

            "funktionstillstand.aktivitet.beskrivning",
            "funktionstillstand.aktivitet.arbetsformaga.motivering",
            "funktionstillstand.aktivitet.arbetsformaga.prognosangivelse",
            "funktionstillstand.aktivitet.arbetsformaga.arbetsuppgift",

            "sysselsattning.nuvarandeArbete",
            "sysselsattning.arbetsloshet",
            "sysselsattning.foraldrarledighet",

            "arbetsformaga.nedsattningsgrad.1/4.from",
            "arbetsformaga.nedsattningsgrad.1/4.tom",
            "arbetsformaga.nedsattningsgrad.1/2.from",
            "arbetsformaga.nedsattningsgrad.1/2.tom",
            "arbetsformaga.nedsattningsgrad.3/4.from",
            "arbetsformaga.nedsattningsgrad.3/4.tom",
            "arbetsformaga.nedsattningsgrad.helt.from",
            "arbetsformaga.nedsattningsgrad.helt.tom",

            "kommentar"
    ]

    static void main(String[] args) {
        def type = "fk7263"
        fileWriter.append(makeRow(headers) + "\n")

        println "Starting anonymization"
        
        int numberOfThreads = args.length > 0 ? Integer.parseInt(args[0]) : 5
        long start = System.currentTimeMillis()
        AnonymiseraConsistentPersonId anonymiseraPersonId = new AnonymiseraConsistentPersonId()
        AnonymiseraHsaId anonymiseraHsaId = new AnonymiseraHsaId()
        AnonymiseraEndastPersonnummerXmlForSRS anonymiseraXml = new AnonymiseraEndastPersonnummerXmlForSRS (anonymiseraPersonId, anonymiseraHsaId)
        def props = new Properties()

        new File("dataSource.properties").withInputStream {
          stream -> props.load(stream)
        }
        def config = new ConfigSlurper().parse(props)
        BasicDataSource dataSource =
            new BasicDataSource(driverClassName: config.dataSource.driver, url: config.dataSource.url,
                                username: config.dataSource.username, password: config.dataSource.password,
                                initialSize: numberOfThreads, maxTotal: numberOfThreads)


        File queryParamFile = new File("queryParameters.json")
        def queryParameters = new JsonSlurper().parseText(queryParamFile.text)

        def hsaIds = queryParameters.hsaIds.collect {"'$it'"}.join(', ')
        def fromDate = queryParameters.fromDate

        def bootstrapSql = new Sql(dataSource)
        def certificateIds = bootstrapSql.rows(
                "select ID from CERTIFICATE where CERTIFICATE_TYPE = :type and CARE_UNIT_ID in ($hsaIds) and SIGNED_DATE >= :fromDate",
                [type : type, fromDate : fromDate])

        bootstrapSql.close()

        println "${certificateIds.size()} certificates found to anonymize"
        final AtomicInteger count = new AtomicInteger(0)
        final AtomicInteger errorCount = new AtomicInteger(0)
        def output
        GParsPool.withPool(numberOfThreads) {
            output = certificateIds.collectParallel {
                StringBuffer result = new StringBuffer() 
                def id = it.ID
                Sql sql = new Sql(dataSource)
                try {
                    sql.withTransaction {
                        // Anonymisera alla befintliga intyg, och deras original-meddelanden
                        def intyg = sql.firstRow( 'select CIVIC_REGISTRATION_NUMBER from CERTIFICATE where ID = :id', [id : id])

                        def original = sql.firstRow( 'select DOCUMENT from ORIGINAL_CERTIFICATE where CERTIFICATE_ID = :id' , [id : id])

                        String xmlDoc
                        if (config.dataSource.driver == "com.mysql.jdbc.Driver") {
                            xmlDoc = original?.DOCUMENT ? new String(original.DOCUMENT, 'UTF-8') : null
                        } else {
                            def blob = (Blob) original.DOCUMENT
                            xmlDoc = new String (blob.getBytes(1L, (int) blob.length()))
                        }

                        String anonymiseradXml = xmlDoc ? anonymiseraXml.anonymiseraIntygsXml(xmlDoc, intyg.CIVIC_REGISTRATION_NUMBER) : null

                        if (anonymiseradXml) {
                            processXml(anonymiseradXml,
                                    anonymiseraPersonId.determineAgeFromPersonnummer(intyg.CIVIC_REGISTRATION_NUMBER),
                                    anonymiseraPersonId.determineSexFromPersonnummer(intyg.CIVIC_REGISTRATION_NUMBER))
                        }
                    }
                    int current = count.addAndGet(1)
                    if (current % 10000 == 0) {
                        println "${current} certificates anonymized in ${(int)((System.currentTimeMillis()-start) / 1000)} seconds"
                    }
                } catch (Throwable t) {
                    result << "Anonymizing ${id} failed: ${t}"
                    errorCount.incrementAndGet()
                } finally {
                    sql.close()
                }
                result.toString()
            }
        }
        long end = System.currentTimeMillis()
        output.each {line ->
            if (line) println line
        }
        println "Done! ${count} certificates anonymized with ${errorCount} errors in ${(int)((end-start) / 1000)} seconds"
        fileWriter.flush()
        fileWriter.close()
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    static void processXml(String xml, String age, String sex) throws ParserConfigurationException, SAXException, IOException,
            XPathExpressionException {

        StringJoiner rowMaker = new StringJoiner("\n")

        def slurper = new XmlSlurper()
        slurper.keepIgnorableWhitespace = true
        def intyg = slurper.parseText(xml)
        intyg.declareNamespace(
                ns1: 'urn:riv:insuranceprocess:healthreporting:mu7263:3',
                ns2: 'urn:riv:insuranceprocess:healthreporting:2',
                ns3: 'urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificateResponder:3')

        String utlatandeId = intyg.'ns3:lakarutlatande'.'ns1:lakarutlatande-id'
        String utlatandeTyp = intyg.'ns3:lakarutlatande'.'ns1:typAvUtlatande'
        utlatandeTyp = utlatandeTyp.replace(",", "")

        String signeringsDatum = intyg.'ns3:lakarutlatande'.'ns1:signeringsdatum'

        String patientId = intyg.'ns3:lakarutlatande'.'ns1:patient'.'ns2:person-id'.@extension

        String personalId = intyg.'ns3:lakarutlatande'.'ns1:skapadAvHosPersonal'.'ns2:personal-id'.@extension

        String enhetsId = intyg.'ns3:lakarutlatande'.'ns1:skapadAvHosPersonal'.'ns2:enhet'.'ns2:enhets-id'.@extension
        String vardgivarId = intyg.'ns3:lakarutlatande'.'ns1:skapadAvHosPersonal'.'ns2:enhet'.'ns2:vardgivare'.'ns2:vardgivare-id'.@extension

        def vardkontaktNodes = intyg.'ns3:lakarutlatande'.'ns1:vardkontakt'
        def referensNodes = intyg.'ns3:lakarutlatande'.'ns1:referens'
        String[] vardkontakter = buildVardkontakter(vardkontaktNodes)
        String[] referenser = buildReferenser(referensNodes)

        def aktiviteter = intyg.'ns3:lakarutlatande'.'ns1:aktivitet'

        String aktivitetSmittskydd = getAktivitet(intyg, 'Avstangning_enligt_SmL_pga_smitta')
        String aktivitetKontaktMedArbetsformedling = getAktivitet(intyg, 'Patienten_behover_fa_kontakt_med_Arbetsformedlingen')

        String aktivitetKontaktForetagsvard = getAktivitet(intyg, 'Patienten_behover_fa_kontakt_med_foretagshalsovarden')

        String aktivitetOvrigt = getAktivitet(intyg, 'Ovrigt')
        String aktivitetOvrigtBeskrivning = getAktivitetBeskrivning(aktiviteter, 'Ovrigt')

        String aktivitetPlaneradPagaendeAtgardSjukvard = getAktivitet(intyg, 'Planerad_eller_pagaende_behandling_eller_atgard_inom_sjukvarden')
        String aktivitetPlaneradPagaendeAtgardSjukvardBeskrivning = getAktivitetBeskrivning(aktiviteter, 'Planerad_eller_pagaende_behandling_eller_atgard_inom_sjukvarden')

        String aktivitetPlaneradPagaendeAnnanAtgard = getAktivitet(intyg, 'Planerad_eller_pagaende_annan_atgard')
        String aktivitetPlaneradPagaendeAnnanAtgardBeskrivning = getAktivitetBeskrivning(aktiviteter, 'Planerad_eller_pagaende_annan_atgard')

        String aktivitetRehabAktuell = getAktivitet(intyg, 'Arbetslivsinriktad_rehabilitering_ar_aktuell')
        String aktivitetRehabEjAktuell = getAktivitet(intyg, 'Arbetslivsinriktad_rehabilitering_ej_aktuell')
        String aktivitetRehabEjBedomma = getAktivitet(intyg, 'Gar_ej_att_bedomma_om_arbetslivsinriktad_rehabilitering_ar_aktuell')

        String aktivitetRessattAktuell = getAktivitet(intyg, 'Forandrat_ressatt_till_arbetsplatsen_ar_aktuellt')
        String aktivitetRessattEjAktuell = getAktivitet(intyg, 'Forandrat_ressatt_till_arbetsplatsen_ar_ej_aktuellt')
        String aktivitetKontaktMedFk = getAktivitet(intyg, 'Kontakt_med_Forsakringskassan_ar_aktuell')

        String medicinskttillstandDiagnoskod = intyg.'ns3:lakarutlatande'.'ns1:medicinsktTillstand'.'ns1:tillstandskod'.@code
        String medicinskttillstandBeskrivning = getBeskrivning(intyg, 'medicinsktTillstand')
        medicinskttillstandDiagnoskod = medicinskttillstandDiagnoskod.replace(",", " ")

        def funkKropp = intyg.'ns3:lakarutlatande'.'ns1:funktionstillstand'.find { it -> it.'ns1:typAvFunktionstillstand'.text() == 'Kroppsfunktion'}
        String funktionstillstandKroppsfunktion = intyg.'ns3:lakarutlatande'.'ns1:funktionstillstand'.'ns1:typAvFunktionstillstand'.find { it -> it.text() == 'Kroppsfunktion'} ? 'true' : 'false'
        String funktionstillstandKroppsfunktionBeskrivning = cleanCommaFromText(funkKropp.'ns1:beskrivning'.text())

        def funkAkt = intyg.'ns3:lakarutlatande'.'ns1:funktionstillstand'.find { it -> it.'ns1:typAvFunktionstillstand'.text() == 'Aktivitet'}
        String funktionstillstandAktivitetBeskrivning
        String arbetsformagaMotivering
        String funktionstillstandPrognos
        String arbetsuppgift

        if (funkAkt != null) {
            funktionstillstandAktivitetBeskrivning = cleanCommaFromText(funkAkt.'ns1:beskrivning'.text())
            arbetsformagaMotivering = cleanCommaFromText(funkAkt.'ns1:arbetsformaga'.'ns1:motivering'.text())
            funktionstillstandPrognos = cleanCommaFromText(intyg.'ns3:lakarutlatande'.'ns1:funktionstillstand'.'ns1:arbetsformaga'.'ns1:prognosangivelse'.text())
            arbetsuppgift = cleanCommaFromText(funkAkt.'ns1:arbetsformaga'.'ns1:arbetsuppgift'.'ns1:typAvArbetsuppgift'.text())
        }

        String bedomttillstandBeskrivning = getBeskrivning(intyg, 'bedomtTillstand')

        def arbetsFormagaNedsattningNodes = intyg.'ns3:lakarutlatande'.'ns1:funktionstillstand'.'ns1:arbetsformaga'.'ns1:arbetsformagaNedsattning'
        String[] arbetsFormagaNedsattningar  = buildArbetsformagaNedsattningar(arbetsFormagaNedsattningNodes)

        String nuvarandeArbete = getSysselsattning(intyg, 'Nuvarande_arbete')
        String arbetsloshet = getSysselsattning(intyg, 'Arbetsloshet')
        String foraldrarledighet = getSysselsattning(intyg, 'Foraldrarledighet')
        String kommentar = cleanCommaFromText(intyg.'ns3:lakarutlatande'.'ns1:kommentar'.text())

        rowMaker.add(makeRow(Arrays.asList(
                utlatandeId, utlatandeTyp, signeringsDatum,
                patientId, age, sex,
                personalId, enhetsId, vardgivarId,
                vardkontakter[0], vardkontakter[1], vardkontakter[2], vardkontakter[3],
                referenser[0], referenser[1], referenser[2], referenser[3],
                aktivitetSmittskydd,
                aktivitetKontaktMedArbetsformedling,
                aktivitetKontaktForetagsvard,

                aktivitetOvrigt,
                aktivitetOvrigtBeskrivning,

                aktivitetPlaneradPagaendeAtgardSjukvard,
                aktivitetPlaneradPagaendeAtgardSjukvardBeskrivning,

                aktivitetPlaneradPagaendeAnnanAtgard,
                aktivitetPlaneradPagaendeAnnanAtgardBeskrivning,

                aktivitetRehabAktuell,

                aktivitetRehabEjAktuell,

                aktivitetRehabEjBedomma,

                aktivitetRessattAktuell,

                aktivitetRessattEjAktuell,

                aktivitetKontaktMedFk,

                medicinskttillstandDiagnoskod,
                medicinskttillstandBeskrivning,

                bedomttillstandBeskrivning,

                funktionstillstandKroppsfunktion,
                funktionstillstandKroppsfunktionBeskrivning,

                funktionstillstandAktivitetBeskrivning,
                arbetsformagaMotivering,
                funktionstillstandPrognos,
                arbetsuppgift,

                nuvarandeArbete,
                arbetsloshet,
                foraldrarledighet,

                arbetsFormagaNedsattningar[0], arbetsFormagaNedsattningar[1],
                arbetsFormagaNedsattningar[2], arbetsFormagaNedsattningar[3],
                arbetsFormagaNedsattningar[4], arbetsFormagaNedsattningar[5],
                arbetsFormagaNedsattningar[6], arbetsFormagaNedsattningar[7],

                kommentar

        )))

        String finalString = rowMaker.toString() + "\n"
        fileWriter.append(finalString)
    }

    static String getSysselsattning(def intyg, String code) {
        return intyg.'ns3:lakarutlatande'.'ns1:funktionstillstand'.'ns1:arbetsformaga'.'ns1:sysselsattning'
                .find{ it -> it.text() == code } ? 'true' : 'false'

    }

    static String getAktivitet(intyg, String code) {
        return intyg.'ns3:lakarutlatande'.'ns1:aktivitet'.'ns1:aktivitetskod'
                .find{ it -> it.text() ==  code } ? 'true' : 'false'
    }

    static String getAktivitetBeskrivning(aktiviteter, String code) {
        def a = aktiviteter.find { v -> v.'ns1:aktivitetskod'.text() == code }
        return cleanCommaFromText(a.'ns1:beskrivning'.text())
    }

    static String getBeskrivning(intyg, String field) {
        return cleanCommaFromText(intyg.'ns3:lakarutlatande'."ns1:${field}".'ns1:beskrivning'.text())
    }

    static String[] buildVardkontakter(vardkontakter) {
        String[] ret = ["", "", "", ""]
        int i = 0

        vardkontakter.each { v ->
            ret[i] = (String) v.'ns1:vardkontakttyp'
            ret[i += 1] = (String) v.'ns1:vardkontaktstid'
            i++
        }
        return ret
    }

    static String[] buildReferenser(referenser) {
        String[] ret = ["", "", "", ""]
        int i = 0
        referenser.each { v ->
            ret[i] = (String) v.'ns1:referenstyp'
            ret[i += 1] = (String) v.'ns1:datum'
            i++
        }
        return ret
    }

    static String[] buildArbetsformagaNedsattningar(nedsattningar) {
        String[] ret = ["", "", "", "", "", "", "", ""]
        nedsattningar.each { v ->
            switch (v.'ns1:nedsattningsgrad') {
                case 'Helt_nedsatt':
                    ret[6] = (String) v.'ns1:varaktighetFrom'
                    ret[7] = (String) v.'ns1:varaktighetTom'
                    break
                case 'Nedsatt_med_1/4':
                    ret[0] = (String) v.'ns1:varaktighetFrom'
                    ret[1] = (String) v.'ns1:varaktighetTom'
                    break
                case 'Nedsatt_med_1/2':
                    ret[2] = (String) v.'ns1:varaktighetFrom'
                    ret[3] = (String) v.'ns1:varaktighetTom'
                    break
                case 'Nedsatt_med_3/4':
                    ret[4] = (String) v.'ns1:varaktighetFrom'
                    ret[5] = (String) v.'ns1:varaktighetTom'
                    break
            }
        }
        return ret
    }

    static String makeRow(List<String> values) {
        StringJoiner stringBuilder = new StringJoiner(", ", "", "")
        for (String s : values) {
            stringBuilder.add(s)
        }
        return stringBuilder.toString()
    }

    static String cleanCommaFromText(String input) {
        return input.replace(",", " ").replace("\n", " ")

    }
}
