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
import org.apache.commons.dbcp2.BasicDataSource
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType

import javax.xml.bind.JAXB
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors
/**
 * Lägg till Sysselsattning på Sjukfall från intyg. Endast för fk7263.
 *
 * @author eriklupander
 */
class UppdateraSjukfall {

    static void main(String[] args) {

        println "- Starting update of SJUKFALL_CERT with EMPLOYMENT info from ORIGINAL_CERTIFICATE"

        int numberOfThreads = args.length > 0 ? Integer.parseInt(args[0]) : 4
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

        println("Fetching all existing sjukfall. This may take several minutes...")
        def certificateIds = bootstrapSql.rows("select ID from SJUKFALL_CERT")

        bootstrapSql.close()

        println "- ${certificateIds.size()} candidates for being updated with sysselsattning"

        final AtomicInteger totalCount = new AtomicInteger(0)
        final AtomicInteger errorCount = new AtomicInteger(0)

        def results

        GParsPool.withPool(numberOfThreads) {

            results = certificateIds.collectParallel {
                StringBuffer result = new StringBuffer()
                def id = it.ID
                Sql sql = new Sql(dataSource)
                try {

                    // Read cert from DB
                    def row = sql.firstRow( 'SELECT c.DOCUMENT FROM ORIGINAL_CERTIFICATE c WHERE c.CERTIFICATE_ID=:id'
                            , [id : id])
                    if (row == null || row.DOCUMENT == null) {
                        println "Intyg ${id} has no DOCUMENT, skipping."
                        throw new Exception("Intyg ${id} has no DOCUMENT, skipping.")
                    }
                    def originalDocument = new String(row.DOCUMENT, 'UTF-8')
                    RegisterMedicalCertificateType registerMedicalCertificate = JAXB.unmarshal(new StringReader(originalDocument), RegisterMedicalCertificateType.class)

                    List<String> sysselsattningar = new ArrayList<>();
                    for (se.inera.ifv.insuranceprocess.healthreporting.mu7263.v3.FunktionstillstandType funktionstillstandType : registerMedicalCertificate.lakarutlatande.funktionstillstand) {
                        if (funktionstillstandType.arbetsformaga != null && funktionstillstandType.arbetsformaga.sysselsattning != null) {
                            for (se.inera.ifv.insuranceprocess.healthreporting.mu7263.v3.SysselsattningType sysselsattning : funktionstillstandType.arbetsformaga.sysselsattning) {
                                sysselsattningar.add(sysselsattning.typAvSysselsattning.value().trim());
                            }
                        }
                    }
                    String sysselsattningStr = sysselsattningar.stream().collect(Collectors.joining(","))
                    if (sysselsattningStr != null && sysselsattningStr.size() > -1) {
                        sql.execute("UPDATE SJUKFALL_CERT sc SET EMPLOYMENT=:sysselsattningStr WHERE sc.id = :id", [sysselsattningStr : sysselsattningStr.toUpperCase(),id : id])
                    } else {
                        throw new IllegalArgumentException("Sysselsattning in cert was null or empty.");
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

        println "- Done! ${totalCount} sjukfall updated with EMPLOYMENT, with ${errorCount} errors in ${(int)((end-start) / 1000)} seconds"

        if (results.size() > 0) {
            println " "
            println "id;message"
            results.each { line ->
                if (line) println line
            }
        }
    }
}
