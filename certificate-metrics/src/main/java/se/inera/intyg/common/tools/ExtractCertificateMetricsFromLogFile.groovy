package se.inera.intyg.common.tools
import groovyx.gpars.GParsPool

import java.util.concurrent.atomic.AtomicInteger
/**
 * Extraherar nödvändig meta-data för intyg från tabellen CERTIFICATE för att få fram
 * underlag som sedan ska användas för att ta fram statistik för hur många intyg som
 * registreras via wiretap och inte via wiretap.
 */
class ExtractCertificateMetricsFromLogFile {

    static void main(String[] args) {

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

        final AtomicInteger count = new AtomicInteger(0)

        def output
        GParsPool.withPool(numberOfThreads) {
            output = rows.collectParallel {
                // Read line and get last item in list
                String last = it.split(",").last()
                String intygId = last.split(" ").first()

                // Increment counter
                count.addAndGet(1)

                // Return certificate id
                intygId
            }
        }

        // Add headers to output
        def header = "intyg-id"
        output.add(0, header)

        // Groovy goodness: make sure no duplicate values exists
        Set uniqueValues = output.toSet()

        def diff = output.size() - uniqueValues.size()
        if (diff > 0) {
            println "${diff} duplicate certificates identities found. Duplicate will not be written to output file."
        }

        // Write content to file
        new FileOutputStream(config.wiretap.output.filePath, false).withWriter(config.wiretap.output.encoding) { writer ->
            uniqueValues.each { line ->
                if (line) {
                    // write the line into the output file
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
