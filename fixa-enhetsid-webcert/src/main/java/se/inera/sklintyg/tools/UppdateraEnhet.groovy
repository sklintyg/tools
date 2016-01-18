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
class UppdateraEnhet {

    static void main(String[] args) {
        int numberOfThreads = args.length > 0 ? Integer.parseInt(args[0]) : 5
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
        def qaIds = bootstrapSql.rows("select internReferens from FRAGASVAR where ENHETS_ID = :enhet", [enhet: config.originalEnhet])
        bootstrapSql.close()
        println "- ${qaIds.size()} questions/answers found"
        final AtomicInteger count = new AtomicInteger(0)
        def output
        GParsPool.withPool(numberOfThreads) {
            output = qaIds.collectParallel {
                StringBuffer result = new StringBuffer() 
                def id = it.internReferens
                Sql sql = new Sql(dataSource)
                sql.execute('update FRAGASVAR set ENHETS_ID = :enhet, VARDGIVAR_ID = :vardgivare where internReferens = :id' , [enhet: config.uppdateradEnhet, vardgivare: config.uppdateradVardgivare, id : id])
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
        println "$count questions/answers updated in ${end-start} milliseconds"
    }

}
