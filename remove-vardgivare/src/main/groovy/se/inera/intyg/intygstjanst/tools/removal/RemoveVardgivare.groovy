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

package se.inera.intyg.tools.removal

import groovy.sql.Sql
import groovyx.gpars.GParsPool
import java.util.concurrent.atomic.AtomicInteger
import org.apache.commons.dbcp2.BasicDataSource

class RemoveVardgivare {
    static void main(String[] args) {

        if (args.length == 0) {
            println "USAGE IS java -jar <jar> <hsaId> [<numberOfThreads>]"
            return
        }
        String hsaId = args[0]
        int numberOfThreads = args.length > 1 ? Integer.parseInt(args[1]) : 5

        long start = System.currentTimeMillis()

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

        // remove all the relevant arende and sjukfall
        bootstrapSql.execute("DELETE FROM ARENDE WHERE INTYGS_ID IN (SELECT ID FROM CERTIFICATE WHERE CARE_GIVER_ID=:hsaId)", [hsaId: hsaId])

        def personNummer = bootstrapSql.rows("SELECT DISTINCT(CIVIC_REGISTRATION_NUMBER) pnr FROM CERTIFICATE WHERE CARE_GIVER_ID=:hsaId", [hsaId: hsaId])

        bootstrapSql.close()

        final AtomicInteger count = new AtomicInteger(0)
        final AtomicInteger errorCount = new AtomicInteger(0)
        GParsPool.withPool(numberOfThreads) {
            personNummer.eachParallel {
                StringBuffer result = new StringBuffer()
                def pnr = it.pnr
                Sql sql = new Sql(dataSource)
                try {
                    sql.withTransaction {
                        def consent = sql.firstRow("select CIVIC_REGISTRATION_NUMBER from CONSENT where CIVIC_REGISTRATION_NUMBER=:pnr", [pnr:pnr.toString().trim()])

                        def certIds = sql.eachRow("SELECT ID FROM CERTIFICATE WHERE CIVIC_REGISTRATION_NUMBER=$pnr AND CARE_GIVER_ID=$hsaId") { row ->
                            sql.execute "DELETE FROM SJUKFALL_CERT_WORK_CAPACITY WHERE CERTIFICATE_ID=:id", [id: row.ID]
                            sql.execute "DELETE FROM SJUKFALL_CERT WHERE ID=:id", [id: row.ID]

                            // This means that there is no "vårdens intyg" and we can remove everything that is associated with the certificates
                            if (consent == null) {
                                sql.execute "DELETE FROM ORIGINAL_CERTIFICATE WHERE CERTIFICATE_ID=:id", [id: row.ID]
                                sql.execute "DELETE FROM CERTIFICATE_STATE WHERE CERTIFICATE_ID=:id", [id: row.ID]
                                sql.execute "DELETE FROM CERTIFICATE WHERE ID=:id", [id: row.ID]
                            }
                            // We should mark these certificates as "DELETED_BY_CARE_GIVER"
                            else {
                                sql.execute "UPDATE CERTIFICATE SET DELETED_BY_CARE_GIVER=1 WHERE ID=:id", [id: row.ID]
                            }
                            int current = count.addAndGet(1)
                            if (current % 100 == 0) {
                                println "${current} certificates handled in ${(int)((System.currentTimeMillis()-start) / 1000)} seconds"
                            }
                        }
                    }
                } catch (Throwable t) {
                    result << "removal for ${pnr} failed: ${t}"
                    errorCount.incrementAndGet()
                } finally {
                    sql.close()
                }
                result.toString()
            }
        }
        long end = System.currentTimeMillis()
        println "Done! ${count} certificates for care has been removed or marked as deleted by care giver with ${errorCount} errors in ${(int)((end-start) / 1000)} seconds"
    }
}
