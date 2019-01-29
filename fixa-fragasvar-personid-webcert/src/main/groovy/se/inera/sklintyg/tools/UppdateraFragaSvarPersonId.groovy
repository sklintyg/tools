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

package se.inera.sklintyg.tools

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.xml.StreamingMarkupBuilder
import groovyx.gpars.GParsPool

import java.util.concurrent.atomic.AtomicInteger

import org.apache.commons.dbcp2.BasicDataSource

/**
 * Ändra enhet och vårdgivare för alla frågor/svar utfärdade på en viss ursprungs-enhet,
 * för att få dessa att matcha med en enhet som det finns hsa-testdata för. 
 */
class UppdateraFragaSvarPersonId {

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
        def fragaSvarSql = bootstrapSql.rows("select DISTINCT(fs.PATIENT_ID) as originalPatientId from FRAGASVAR fs WHERE fs.PATIENT_ID like '%-%'")
        bootstrapSql.close()
        println "- ${fragaSvarSql.size()} personnummer found in FRAGASVAR found with PATIENT_ID containing -"
        final AtomicInteger count = new AtomicInteger(0)
        def output
        GParsPool.withPool(numberOfThreads) {
            output = fragaSvarSql.collectParallel {
                StringBuffer result = new StringBuffer() 
                def originalPatientId = it.originalPatientId

                // Make sure it seems to be a valid Personnummer
                if (originalPatientId.length() == 13 && originalPatientId.contains('-')) {
                    def newPatientId = originalPatientId.replaceAll('-', '')
                    Sql sql = new Sql(dataSource)
                    sql.execute('update FRAGASVAR fs set fs.PATIENT_ID = :newPatientId WHERE fs.PATIENT_ID =:originalPatientId' , [newPatientId: newPatientId, originalPatientId: originalPatientId])
                    sql.close()
                    int current = count.addAndGet(1)
                    if (current % 100 == 0) {
                        println current
                    }
                    result.toString()
                }
            }
        }
        long end = System.currentTimeMillis()
        output.each {line ->
            if (line) println line
        }
        println "$count patientId's updated in ${end-start} milliseconds"
    }

}
