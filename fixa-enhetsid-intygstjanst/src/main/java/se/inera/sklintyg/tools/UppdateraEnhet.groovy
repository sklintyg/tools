package se.inera.sklintyg.tools

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.xml.StreamingMarkupBuilder
import groovyx.gpars.GParsPool

import java.util.concurrent.atomic.AtomicInteger

import org.apache.commons.dbcp2.BasicDataSource

/**
 * Ändra enhet och vårdgivare för alla intyg utfärdade på en viss ursprungs-enhet,
 * för att få dessa att matcha med en enhet som det finns hsa-testdata för. 
 */
class UppdateraEnhet {

    static void main(String[] args) {
        int numberOfThreads = args.length > 0 ? args[0] : 5
        long start = System.currentTimeMillis()
        def props = new Properties()
        new File("dataSource.properties").withInputStream { stream ->
            props.load(stream)
        }
        new File("hsa.properties").withInputStream { stream ->
            props.load(stream)
        }
        def config = new ConfigSlurper().parse(props)
        String uppdateradEnhet = config.uppdateradEnhet
        String uppdateradVardgivare = config.uppdateradVardgivare
        BasicDataSource dataSource =
            new BasicDataSource(driverClassName: config.dataSource.driver, url: config.dataSource.url,
                                username: config.dataSource.username, password: config.dataSource.password,
                                initialSize: numberOfThreads, maxTotal: numberOfThreads)
        def bootstrapSql = new Sql(dataSource)
        def certificateIds = bootstrapSql.rows("select ID from CERTIFICATE where CARE_UNIT_ID = :enhet", [enhet: config.originalEnhet])
        bootstrapSql.close()
        println "- ${certificateIds.size()} certificates found"
        final AtomicInteger count = new AtomicInteger(0)
        def output
        GParsPool.withPool(numberOfThreads) {
            output = certificateIds.collectParallel {
                StringBuffer result = new StringBuffer() 
                def id = it.ID
                Sql sql = new Sql(dataSource)
                def row = sql.firstRow( 'select DOCUMENT from CERTIFICATE where ID = :id' , [id : id])
                def jsonDocument = new JsonSlurper().parseText(new String(row.DOCUMENT, 'UTF-8'))
                jsonDocument.skapadAv.vardenhet.id.extension = config.uppdateradEnhet
                jsonDocument.skapadAv.vardenhet.vardgivare.id.extension = config.uppdateradVardgivare
                def updatedJson = new JsonBuilder(jsonDocument).toString()
                sql.execute('update CERTIFICATE set DOCUMENT = :document, CARE_UNIT_ID = :enhet where ID = :id' , [document: updatedJson.getBytes('UTF-8'), enhet: config.uppdateradEnhet, id : id])
                row = sql.firstRow( 'select DOCUMENT from ORIGINAL_CERTIFICATE where CERTIFICATE_ID = :id' , [id : id])
                def slurper = new XmlSlurper()
                slurper.keepIgnorableWhitespace = true
                def xmlDocument = slurper.parseText(new String(row.DOCUMENT, 'UTF-8'))
                xmlDocument.declareNamespace(ns1: 'urn:riv:insuranceprocess:healthreporting:mu7263:3',
                                       ns2: 'urn:riv:insuranceprocess:healthreporting:2',
                                       ns3: 'urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificateResponder:3')
                xmlDocument.'ns3:lakarutlatande'.'ns1:skapadAvHosPersonal'.'ns2:enhet'.'ns2:enhets-id'.@extension = config.uppdateradEnhet
                xmlDocument.'ns3:lakarutlatande'.'ns1:skapadAvHosPersonal'.'ns2:enhet'.'ns2:vardgivare'.'ns2:vardgivare-id'.@extension = config.uppdateradVardgivare
                def outputBuilder = new StreamingMarkupBuilder()
                outputBuilder.encoding = 'UTF-8'
                def updatedXml = outputBuilder.bind{  mkp.yield xmlDocument }.toString()
                sql.execute('update ORIGINAL_CERTIFICATE set DOCUMENT = :document where CERTIFICATE_ID = :id' , [document: updatedXml.getBytes('UTF-8'), id : id])
                sql.close()
                int current = count.addAndGet(1)
                if (current % 100 == 0) {
                    println current
                }
                result.toString()
            }
        }
        long end = System.currentTimeMillis()
        output.each {line ->
            if (line) println line
        }
        println "$count certificates updated in ${end-start} milliseconds"
    }

}
