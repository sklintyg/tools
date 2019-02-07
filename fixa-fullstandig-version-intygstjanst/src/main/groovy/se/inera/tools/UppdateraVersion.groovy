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

package se.inera.tools

import groovy.sql.Sql
import groovyx.gpars.GParsPool
import java.util.concurrent.atomic.AtomicInteger
import org.apache.commons.dbcp2.BasicDataSource

/**
 * Uppdatera version i berörda intygstyper till att omfatta major och minor.
 *
 */
class UppdateraVersion {
    static void main(String[] args) {
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
        def certificates = bootstrapSql.rows("select ID from CERTIFICATE where CERTIFICATE_TYPE in ('ts-bas', 'ts-diabetes', 'luse')")
        bootstrapSql.close()
        println "- ${certificates.size()} certificates of type ts-bas, ts-diabetes or luse found"
        final AtomicInteger count = new AtomicInteger(0)
        def output
        GParsPool.withPool(numberOfThreads) {
            output = certificates.collectParallel {
                StringBuffer result = new StringBuffer() 
                def id = it.ID

                Sql sql = new Sql(dataSource)
                def original = sql.firstRow('select DOCUMENT from ORIGINAL_CERTIFICATE where CERTIFICATE_ID = :id', [id: id])
                String xmlDoc = original?.DOCUMENT ? new String(original.DOCUMENT, 'UTF-8') : null
                String newVersion = getVersionFromXml(xmlDoc)
                if (newVersion == null) {
                    println "Could not parse version for certificate_id: ${id}"
                } else {
                    sql.execute('update CERTIFICATE set CERTIFICATE_TYPE_VERSION = :version where ID = :id', [version: newVersion, id: id])
                }
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

    static String getVersionFromXml(String xml) {
        def slurper = new XmlSlurper(false, true)
        slurper.keepIgnorableWhitespace = true
        def intyg = slurper.parseText(xml)
        if (xml.contains('RegisterCertificate')) {
            return intyg.'intyg'.'version'
        }
        else {
            String version = ""
            String utgava = ""
            if (xml.contains('RegisterTSBasResponder')) {
                intyg.declareNamespace(
                        '': 'urn:local:se:intygstjanster:services:1',
                        ns2: 'urn:local:se:intygstjanster:services:types:1',
                        ns3: 'urn:local:se:intygstjanster:services:RegisterTSBasResponder:1')
                version = intyg.'ns3:intyg'.'version'
                utgava = intyg.'ns3:intyg'.'utgava'
                try {
                    return Integer.parseInt(version) + "." + Integer.parseInt(utgava)
                } catch (NumberFormatException ne) {
                    println("Failed to parse version or utgava for ${intyg.'ns3:intyg:'.'intygsId'} \n"
                            + "XML was: ${intyg} \n"
                            + "Stacktrace ${ne.getStackTrace()}")
                    return "6.7"
                }
            } else if (xml.contains('RegisterTSDiabetesResponder')) {
                intyg.declareNamespace(
                        '': 'urn:local:se:intygstjanster:services:1',
                        ns2: 'urn:local:se:intygstjanster:services:types:1',
                        ns3: 'urn:local:se:intygstjanster:services:RegisterTSDiabetesResponder:1')
                version = intyg.'ns3:intyg'.'version'
                utgava = intyg.'ns3:intyg'.'utgava'
                try {
                    return Integer.parseInt(version) + "." + Integer.parseInt(utgava)
                } catch (NumberFormatException ne) {
                    println("Failed to parse version or utgava for ${intyg.'ns3:intyg:'.'intygsId'} \n"
                            + "XML was: ${intyg} \n"
                            + "Stacktrace ${ne.getStackTrace()}")
                    return "2.6"
                }
            }
        }

    }

}
