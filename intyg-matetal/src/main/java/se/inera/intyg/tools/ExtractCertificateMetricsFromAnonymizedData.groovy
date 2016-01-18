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

package se.inera.intyg.tools

import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovyx.gpars.GParsPool
import org.apache.commons.dbcp2.BasicDataSource

import java.util.concurrent.atomic.AtomicInteger
/**
 * Extraherar nödvändig meta-data för intyg från tabellen CERTIFICATE för att få fram
 * underlag som sedan ska användas för att ta fram statistik för hur många intyg som
 * registreras via wiretap och inte via wiretap.
 */
class ExtractCertificateMetricsFromAnonymizedData {

    final static CSV_DELIMITER = '|'

    static void main(String[] args) {

        // Read configuration
        def props = new Properties()
        new File("dataSource.properties").withInputStream {
            stream -> props.load(stream)
        }
        def config = new ConfigSlurper().parse(props)

        println "Start extraction of certificate metrics from anonymized database."

        long start = System.currentTimeMillis()
        int  numberOfThreads = args.length > 0 ? args[0] : 5

        // Setup data source
        BasicDataSource dataSource =
                new BasicDataSource(driverClassName: config.metrics.dataSource.driver, url: config.metrics.dataSource.url,
                        username: config.metrics.dataSource.username, password: config.metrics.dataSource.password,
                        initialSize: numberOfThreads, maxTotal: numberOfThreads)

        def bootstrapSql = new Sql(dataSource)

        // Get all certificate IDs
        println "Fetching certificate identities between the dates '${config.metrics.fromDateTime}' and '${config.metrics.toDateTime}'"
        def query = "select ID from CERTIFICATE where SIGNED_DATE between '${config.metrics.fromDateTime}' and '${config.metrics.toDateTime}'".toString()
        def certificateIds = bootstrapSql.rows(query)
        println "${certificateIds.size()} certificates found"

        // Close sql connection
        bootstrapSql.close()
        // -- End Bootstrap --

        final AtomicInteger count = new AtomicInteger(0)
        final AtomicInteger errorCount = new AtomicInteger(0)

        def output

        GParsPool.withPool(numberOfThreads) {
            output = certificateIds.collectParallel {
                def list = []
                Sql sql = new Sql(dataSource)

                try {
                    // Read row by row
                    def row = sql.firstRow('select DOCUMENT from CERTIFICATE where ID = :id' , [id : it.ID])

                    // Get anonymized certificate which is in JSON format
                    def jsonDocument = new JsonSlurper().parseText(new String(row.DOCUMENT, 'UTF-8'))

                    // Populate list
                    list << it.ID
                    list << jsonDocument.skapadAv.vardenhet.vardgivare.id.extension?.trim()
                    list << jsonDocument.skapadAv.vardenhet.vardgivare.namn?.trim()
                    list << jsonDocument.skapadAv.vardenhet.id.extension?.trim()
                    list << jsonDocument.skapadAv.vardenhet.namn?.trim()
                    list << jsonDocument.skapadAv.vardenhet.postnummer?.trim()
                    list << jsonDocument.skapadAv.vardenhet.postort?.trim()

                    //println list
                    int current = count.addAndGet(1)
                    if (current % 10000 == 0) {
                        println "${current} certificates processed..."
                    }

                } catch (Throwable t) {
                    println "ERROR: Extracting metrics from certificate ${id} failed: ${t}"
                    errorCount.incrementAndGet()
                } finally {
                    sql.close()
                }

                list.join(CSV_DELIMITER)
            }
        }

        // Add headers to output
        def header = ["intyg-id", "vardgivare-id", "vardgivare-namn", "vardenhet-id", "vardenhet-namn", "postnummer", "postort"]
        output.add(0, header.join(CSV_DELIMITER))

        // Write content to file
        new FileOutputStream(config.metrics.output.filePath, false).withWriter(config.metrics.output.encoding) { writer ->
            output.each { line ->
                if (line) {
                    // write the line into the output file
                    writer << line + "\n"
                }
            }
        }

        long end = System.currentTimeMillis()
        println "${count} certificates extracted in ${(int)((end-start) / 1000)} seconds."
        println "${errorCount} certificates failed. See print-out for certificate identities (if any)."
        println "Output file is '${config.metrics.output.filePath}'"
        println "Done!"
    }

}
