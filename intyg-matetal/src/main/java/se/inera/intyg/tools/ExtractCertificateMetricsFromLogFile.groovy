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
import groovyx.gpars.GParsPool

import java.util.concurrent.atomic.AtomicInteger
/**
 * Extraherar nödvändig meta-data för intyg från tabellen CERTIFICATE för att få fram
 * underlag som sedan ska användas för att ta fram statistik för hur många intyg som
 * registreras via wiretap och inte via wiretap.
 */
class ExtractCertificateMetricsFromLogFile {


    static void main(String[] args) {

        /*
            Adds a .fromString() method to the Date class that parses
            just about anything you can throw at it
         */
        Date.metaClass.'static'.fromString = { str ->
            com.mdimension.jchronic.Chronic.parse(str).beginCalendar.time
        }

        // Read configuration
        def props = new Properties()
        new File("dataSource.properties").withInputStream {
            stream -> props.load(stream)
        }
        def config = new ConfigSlurper().parse(props)

        println "Start extraction of certificate identities from wiretap logfile."
        println "Input file is '${config.wiretap.input.filePath}'"

        long start = System.currentTimeMillis()
        int  numberOfThreads = args.length > 0 ? args[0] : 5

        // Setup data source
        def rows = new ArrayList<String>()
        new File(config.wiretap.input.filePath).withInputStream {
            stream -> rows.addAll(stream.readLines())
        }
        println "${rows.size()} lines in logfile found"

        def dt_from = config.metrics.fromDateTime
        def dt_to = config.metrics.toDateTime
        final AtomicInteger count = new AtomicInteger(0)

        def output
        GParsPool.withPool(numberOfThreads) {
            output = rows
                    .findAllParallel{ r ->
                        def dt = r.split(",").first()
                        Date.fromString(dt) > Date.fromString(dt_from) && Date.fromString(dt) < Date.fromString(dt_to)
                    }
                    .collectParallel { r ->
                        // Read line and get last item in list
                        String intygId = r.split(",").last().split(" ").first()

                        // Increment counter
                        count.addAndGet(1)

                        // Return certificate id
                        intygId
                    }
        }

        def diff = rows.size() - output.size()
        if (diff > 0) {
            println "${diff} certificates identities were outside the specified date range ${dt_from} to ${dt_to}"
        }

        // Groovy goodness: make sure no duplicate values exists
        Set uniqueValues = output.toSet()

        diff = output.size() - uniqueValues.size()
        if (diff > 0) {
            println "${diff} duplicate certificates identities found. Duplicate will not be written to output file."
        }

        // Write to file
        new FileOutputStream(config.wiretap.output.filePath, false).withWriter(config.wiretap.output.encoding) { writer ->
            // Write header
            writer << "intyg-id" + "\n"

            // Write content
            uniqueValues.each { line ->
                if (line) {
                    writer << line + "\n"
                }
            }
        }

        long end = System.currentTimeMillis()
        println "${count} certificate identities extracted in ${(int)((end-start) / 1000)} seconds."
        println "${count-diff} certificate identities was written to file."
        println "Output file is '${config.wiretap.output.filePath}'"
        println "Done!"
    }

}
