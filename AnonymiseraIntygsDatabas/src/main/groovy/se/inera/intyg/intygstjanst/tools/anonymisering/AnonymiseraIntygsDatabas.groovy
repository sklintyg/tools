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

package se.inera.intyg.intygstjanst.tools.anonymisering

import groovy.sql.Sql
import groovyx.gpars.GParsPool

import java.util.concurrent.atomic.AtomicInteger

import org.apache.commons.dbcp2.BasicDataSource

import se.inera.intyg.common.tools.anonymisering.AnonymiseraDatum;
import se.inera.intyg.common.tools.anonymisering.AnonymiseraHsaId;
import se.inera.intyg.common.tools.anonymisering.AnonymiseraJson;
import se.inera.intyg.common.tools.anonymisering.AnonymiseraPersonId;
import se.inera.intyg.common.tools.anonymisering.AnonymiseraXml;
import se.inera.intyg.common.tools.anonymisering.AnonymizeString;

class AnonymiseraIntygsDatabas {

    static void main(String[] args) {
        println "Starting anonymization"
        
        int numberOfThreads = args.length > 0 ? Integer.parseInt(args[0]) : 5
        long start = System.currentTimeMillis()
        AnonymiseraPersonId anonymiseraPersonId = new AnonymiseraPersonId()
        AnonymiseraHsaId anonymiseraHsaId = new AnonymiseraHsaId()
        AnonymiseraDatum anonymiseraDatum = new AnonymiseraDatum()
        AnonymiseraJson anonymiseraJson = new AnonymiseraJson(anonymiseraHsaId, anonymiseraDatum)
        AnonymiseraXml anonymiseraXml = new AnonymiseraXml(anonymiseraPersonId, anonymiseraHsaId, anonymiseraDatum)
        def props = new Properties()
        new File("dataSource.properties").withInputStream {
          stream -> props.load(stream)
        }
        def config = new ConfigSlurper().parse(props)
        BasicDataSource dataSource =
            new BasicDataSource(driverClassName: config.dataSource.driver, url: config.dataSource.url,
                                username: config.dataSource.username, password: config.dataSource.password,
                                initialSize: numberOfThreads, maxTotal: numberOfThreads)
        def bootstrapSql = new Sql(dataSource)

        // For now, just get fk7263
        def desiredTypes = ["fk7263"]

        // Arende is not anonymized, so purge the whole table
        bootstrapSql.execute("TRUNCATE ARENDE")
        bootstrapSql.execute("TRUNCATE CONSENT")
        bootstrapSql.execute("TRUNCATE SJUKFALL_CERT_WORK_CAPACITY")
        bootstrapSql.execute("TRUNCATE SJUKFALL_CERT")

        def certificateIds = bootstrapSql.rows("select ID, CERTIFICATE_TYPE from CERTIFICATE")

        bootstrapSql.close()
        println "${certificateIds.size()} certificates found to anonymize"
        final AtomicInteger count = new AtomicInteger(0)
        final AtomicInteger errorCount = new AtomicInteger(0)

        def output
        GParsPool.withPool(numberOfThreads) {
            output = certificateIds.collectParallel {
                StringBuffer result = new StringBuffer() 
                def id = it.ID
                def currentType = it.CERTIFICATE_TYPE
                Sql sql = new Sql(dataSource)
                try {
                    sql.withTransaction {
                        if (currentType in desiredTypes) {
                            // Anonymisera alla befintliga intyg, och deras original-meddelanden
                            def intyg = sql.firstRow('select CIVIC_REGISTRATION_NUMBER, SIGNING_DOCTOR_NAME from CERTIFICATE where ID = :id', [id: id])

                            String civicRegistrationNumber = anonymiseraPersonId.anonymisera(intyg.CIVIC_REGISTRATION_NUMBER)
                            String signingDoctorName = AnonymizeString.anonymize(intyg.SIGNING_DOCTOR_NAME)

                            sql.executeUpdate('update CERTIFICATE set CIVIC_REGISTRATION_NUMBER = :civicRegistrationNumber, SIGNING_DOCTOR_NAME = :signingDoctorName where ID = :id',
                                    [civicRegistrationNumber: civicRegistrationNumber, signingDoctorName: signingDoctorName, id: id])

                            def original = sql.firstRow('select DOCUMENT from ORIGINAL_CERTIFICATE where CERTIFICATE_ID = :id', [id: id])
                            String xmlDoc = original?.DOCUMENT ? new String(original.DOCUMENT, 'UTF-8') : null
                            String anonymiseradXml = xmlDoc ? anonymiseraXml.anonymiseraIntygsXml(xmlDoc, civicRegistrationNumber) : null
                            if (anonymiseradXml) sql.executeUpdate('update ORIGINAL_CERTIFICATE set DOCUMENT = :document where CERTIFICATE_ID = :id',
                                    [document: anonymiseradXml.getBytes('UTF-8'), id: id])
                        } else {
                            // We are not anonymizing this type of intyg, thus it has to be purged from the database
                            sql.execute("DELETE FROM ORIGINAL_CERTIFICATE WHERE CERTIFICATE_ID=:id", [id: id])
                            sql.execute("DELETE FROM CERTIFICATE_STATE WHERE CERTIFICATE_ID=:id", [id: id])
                            sql.execute("DELETE FROM CERTIFICATE WHERE ID=:id", [id: id])
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
    }
}
