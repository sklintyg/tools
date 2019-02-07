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

import groovy.json.JsonSlurper
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
        def certificates = bootstrapSql.rows("select INTYGS_ID, MODEL from INTYG where INTYGS_TYP in ('ts-bas', 'ts-diabetes', 'luse')")
        bootstrapSql.close()
        println "- ${certificates.size()} certificates of type ts-bas, ts-diabetes or luse found"
        final AtomicInteger count = new AtomicInteger(0)
        def output
        GParsPool.withPool(numberOfThreads) {
            output = certificates.collectParallel {
                StringBuffer result = new StringBuffer() 
                def id = it.INTYGS_ID
                String newVersion = getVersionFromJson(new String(it.MODEL, 'utf-8'))
                if (newVersion == null) {
                    println "Could not parse version for certificate_id: ${id}"
                } else {
                    Sql sql = new Sql(dataSource)
                    sql.execute('update INTYG set INTYG_TYPE_VERSION = :version where INTYGS_ID = :id', [version: newVersion, id: id])
                    sql.close()
                }
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

    static String getVersionFromJson(String s) {
        def intyg = new JsonSlurper().parseText(s)
        def version = intyg.textVersion
        if (version == null) {
            if (intyg.typ == 'ts-bas') {
                version = 6.7
            } else if (intyg.typ == 'ts-diabetes') {
                version = 2.6
            }
        }
        return version
    }

}
