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

package se.inera.intyg.tools.sjukfall

import groovy.sql.Sql
import groovyx.gpars.GParsPool

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger
import java.io.StringReader

import javax.xml.bind.JAXB

import org.apache.commons.dbcp2.BasicDataSource

import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType
import se.inera.ifv.insuranceprocess.healthreporting.mu7263.v3.ArbetsformagaNedsattningType
import se.inera.ifv.insuranceprocess.healthreporting.mu7263.v3.FunktionstillstandType
import se.inera.ifv.insuranceprocess.healthreporting.mu7263.v3.Nedsattningsgrad

/**
 * Skapa Sjukfall av intyg.
 *
 * @author eriklupander
 */
class SkapaSjukfall {

    static void main(String[] args) {

        println "- Starting Intyg -> Sjukfall creation"

        int numberOfThreads = args.length > 0 ? Integer.parseInt(args[0]) : 5
        long start = System.currentTimeMillis()
        def props = new Properties()
        new File("dataSource.properties").withInputStream { stream ->
            props.load(stream)
        }
        def config = new ConfigSlurper().parse(props)

        BasicDataSource dataSource =
            new BasicDataSource(driverClassName: config.dataSource.driver, url: config.dataSource.url,
                                username: config.dataSource.username, password: config.dataSource.password,
                                initialSize: numberOfThreads, maxTotal: numberOfThreads)
        def bootstrapSql = new Sql(dataSource)

        println("Fetching all FK7263 certificates. This may take several minutes...")
        def certificateIds = bootstrapSql.rows("select c.ID, cs.STATE from CERTIFICATE c LEFT OUTER JOIN CERTIFICATE_STATE cs ON cs.CERTIFICATE_ID=c.ID  AND cs.STATE = 'CANCELLED' WHERE c.CERTIFICATE_TYPE = :certType",      //  AND DELETED = :deleted
                                               [certType : 'fk7263'])                                                      // , deleted : false


        bootstrapSql.close()

        println "- ${certificateIds.size()} candidates for being processed into sjukfall found"

        final AtomicInteger totalCount = new AtomicInteger(0)
        final AtomicInteger errorCount = new AtomicInteger(0)

        def results

        GParsPool.withPool(numberOfThreads) {

            results = certificateIds.collectParallel {
                StringBuffer result = new StringBuffer()
                def id = it.ID
                def cancelled = it.STATE
                Sql sql = new Sql(dataSource)
                try {
                    def row = sql.firstRow( 'SELECT DOCUMENT FROM ORIGINAL_CERTIFICATE c WHERE c.CERTIFICATE_ID=:id'
                            , [id : id])
                    if (row == null || row.DOCUMENT == null) {
                        println "Intyg ${id} has no DOCUMENT, skipping."
                        throw new Exception("Intyg ${id} has no DOCUMENT, skipping.")
                    }
                    def originalDocument = new String(row.DOCUMENT, 'UTF-8')
                    RegisterMedicalCertificateType registerMedicalCertificate = JAXB.unmarshal(new StringReader(originalDocument), RegisterMedicalCertificateType.class)

                    String diagnosKod = registerMedicalCertificate.lakarutlatande.medicinsktTillstand?.tillstandskod?.code
                    if (diagnosKod == null) {
                        println "Intyg ${id} has no diagnosKod, skipping."
                        throw new Exception("Intyg ${id} has no diagnosKod, skipping.")
                    }

                    String personnummer = registerMedicalCertificate.lakarutlatande.patient.personId.extension
                    String patientName = registerMedicalCertificate.lakarutlatande.patient.fullstandigtNamn
                    String careUnitId = registerMedicalCertificate.lakarutlatande.skapadAvHosPersonal.enhet.enhetsId.extension
                    String careUnitName = registerMedicalCertificate.lakarutlatande.skapadAvHosPersonal.enhet.enhetsnamn
                    String careGiverId = registerMedicalCertificate.lakarutlatande.skapadAvHosPersonal.enhet.vardgivare.vardgivareId.extension
                    String doctorId = registerMedicalCertificate.lakarutlatande.skapadAvHosPersonal.personalId.extension
                    String doctorName = registerMedicalCertificate.lakarutlatande.skapadAvHosPersonal.fullstandigtNamn

                    Boolean deleted = cancelled != null

                    LocalDateTime signingDateTime = registerMedicalCertificate.lakarutlatande.signeringsdatum
                    java.sql.Date sqlSigningDateTime = new java.sql.Date(signingDateTime.atZone(ZoneId.of("Europe/Stockholm")).toEpochSecond())

                    def sjukfallRow = sql.firstRow( 'SELECT id FROM SJUKFALL_CERT sc WHERE sc.id = :id', [id : id])

                    // If not exists, just insert.
                    if (sjukfallRow == null || sjukfallRow.ID == null) {

                        // Insert base sjukfall cert data
                        sql.execute("INSERT INTO SJUKFALL_CERT "
                                + "(ID,CERTIFICATE_TYPE,CIVIC_REGISTRATION_NUMBER,PATIENT_NAME,CARE_UNIT_ID,CARE_UNIT_NAME,CARE_GIVER_ID,SIGNING_DOCTOR_ID,SIGNING_DOCTOR_NAME,DIAGNOSE_CODE,DELETED,SIGNING_DATETIME)"
                                + "VALUES (:id,:type,:personnummer,:patientName,:careUnitId,:careUnitName,:careGiverId,:doctorId,:doctorName,:diagnosKod,:deleted,:signingDateTime)"
                                , [id: id, type : registerMedicalCertificate.lakarutlatande.typAvUtlatande, personnummer: personnummer, patientName : patientName.trim(),
                                   careUnitId : careUnitId, careUnitName : careUnitName, careGiverId : careGiverId, doctorId : doctorId,
                                   doctorName : doctorName, diagnosKod : diagnosKod, deleted : deleted, signingDateTime : sqlSigningDateTime
                        ])

                        // Insert one item per nedsattning
                        String insertSql = "INSERT INTO SJUKFALL_CERT_WORK_CAPACITY (CERTIFICATE_ID,CAPACITY_PERCENTAGE,FROM_DATE,TO_DATE) VALUES(:id,:nedsattningProcent,:fromDate,:toDate)";

                        for (FunktionstillstandType funktionstillstand : registerMedicalCertificate.lakarutlatande.funktionstillstand) {
                            if (funktionstillstand.arbetsformaga == null) {
                                continue;
                            }
                            for (ArbetsformagaNedsattningType nedsattning : funktionstillstand.arbetsformaga.arbetsformagaNedsattning) {
                                if (nedsattning.nedsattningsgrad != null && (nedsattning.varaktighetFrom != null && nedsattning.varaktighetTom != null)) {
                                    def nedsattningProcent
                                    if (nedsattning.nedsattningsgrad == Nedsattningsgrad.HELT_NEDSATT) {
                                        nedsattningProcent = 100
                                    } else if (nedsattning.nedsattningsgrad == Nedsattningsgrad.NEDSATT_MED_3_4) {
                                        nedsattningProcent = 75
                                    } else if (nedsattning.nedsattningsgrad == Nedsattningsgrad.NEDSATT_MED_1_2) {
                                        nedsattningProcent = 50
                                    } else if (nedsattning.nedsattningsgrad == Nedsattningsgrad.NEDSATT_MED_1_4) {
                                        nedsattningProcent = 25
                                    }
                                    if (nedsattningProcent != null) {
                                        sql.execute(insertSql, [id:id,
                                                nedsattningProcent: nedsattningProcent,
                                                fromDate: nedsattning.varaktighetFrom.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                                toDate: nedsattning.varaktighetTom.format(DateTimeFormatter.ISO_LOCAL_DATE)])
                                    }
                                }
                            }
                        }
                    } else {
                        // If SJUKFALL_CERT already exists for id, just update the DELETED state.
                        sql.execute( 'UPDATE SJUKFALL_CERT sc SET sc.deleted = :deleted WHERE id = :id', [deleted : deleted, id : id])
                    }


                } catch (Exception e) {

                    result << "${id};${e.message}"
                    errorCount.incrementAndGet()
                }
                sql.close()

                int current = totalCount.incrementAndGet()

                if (current % 1000 == 0) {
                    println "- ${current} certificates processed in ${(int)((System.currentTimeMillis()-start) / 1000)} seconds, ${errorCount} errors"
                }
                result.toString()
            }
        }

        long end = System.currentTimeMillis()

        println "- Done! ${totalCount} certificates processed into sjukfall with ${errorCount} errors in ${(int)((end-start) / 1000)} seconds"

        if (results.size() > 0) {
            println " "
            println "id;message"
            results.each { line ->
                if (line) println line
            }
        }

    }

}
