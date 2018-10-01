/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import groovy.sql.Sql
import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.commons.dbcp2.BasicDataSource

import javax.jms.*
import java.util.concurrent.atomic.AtomicInteger
/**
 * Script for putting all intyg found in Intygtjänsten on a specific JMS queue.
 */
class SkickaIntygTillIntygsstatistik {

    static final JMS_ACTION_CREATED = "created"
    static final JMS_ACTION_REVOKED = "revoked"
    static final JMS_PARAM_CERTIFICATE_ID = "certificate-id"
    static final JMS_PARAM_ACTION = "action"

    static final SQL_INTYG_PAYLOAD = "SELECT DOCUMENT FROM ORIGINAL_CERTIFICATE WHERE ID = :id"
    static final SQL_INTYG_REVOKED = "SELECT COUNT(CERTIFICATE_ID) AS REVOKED FROM CERTIFICATE_STATE WHERE CERTIFICATE_ID = :id AND STATE = 'CANCELLED'"

    final AtomicInteger totalCount = new AtomicInteger(0)
    final AtomicInteger processedCount = new AtomicInteger(0)
    final AtomicInteger createMsgCount = new AtomicInteger(0)
    final AtomicInteger revokeMsgCount = new AtomicInteger(0)

    long startTime = 0;

    ConfigObject appConf = null

    BasicDataSource dataSource = null
    Connection jmsConnection = null
    Session jmsSession = null
    Destination jmsDestination = null

    public SkickaIntygTillIntygsstatistik() {
        this(null)
    }

    public SkickaIntygTillIntygsstatistik(String fileName) {
        Properties appProperties = new Properties()

        if (fileName) {
            this.getClass().getResource('/' + fileName).withInputStream {
                appProperties.load(it)
            }
        }

        // Init application's default configuration
        appConf = new ConfigSlurper().parse(appProperties)
        appConf.debug = true
    }

    def setupDataSource() {
        if (appConf.debug.toBoolean()) println 'setupDataSource()'

        dataSource = new BasicDataSource(driverClassName: appConf.db.driver, url: appConf.db.url,
                username: appConf.db.username, password: appConf.db.password, initialSize: 1, maxTotal: 1)
    }

    def setupJMS() {
        if (appConf.debug.toBoolean()) println 'setupJMS()'

        if (appConf.broker.username) {
            jmsConnection = new ActiveMQConnectionFactory(userName: appConf.broker.username, password: appConf.broker.password,
                    brokerURL: appConf.broker.url).createConnection()
        } else {
            // Try anonymous login
            jmsConnection = new ActiveMQConnectionFactory(brokerURL: appConf.broker.url).createConnection()
        }
        jmsSession = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE)
        jmsDestination = jmsSession.createQueue(appConf.broker.queue)
    }

    def tearDownJMS() {
        if (appConf.debug.toBoolean()) println 'tearDownJMS()'

        if (jmsSession) {
            jmsSession.close()
        }
        if (jmsConnection) {
            jmsConnection.close()
        }
    }

    def getIntyg(def intygId) {
        //if (appConf.debug.toBoolean()) println 'getIntyg(' + intygId + ')'

        Intyg intygObj = new Intyg(intygId)

        def sql = new Sql(dataSource)

        def payloadRow = sql.firstRow(SQL_INTYG_PAYLOAD, [id : intygId])
        if (payloadRow) {
            intygObj.contents = new String(payloadRow.DOCUMENT, 'UTF-8')
        }

        def revokedRow = sql.firstRow(SQL_INTYG_REVOKED, [id : intygId])
        if (revokedRow) {
            intygObj.revoked = (revokedRow.REVOKED != 0)
        }

        sql.close()
        return intygObj
    }

    def sendToQueue(Intyg intyg) {
        //if (appConf.debug.toBoolean()) println 'sendToQueue(' + intyg + ')'

        Message message = createMessage(jmsSession, intyg, false)
        MessageProducer msgProducer = jmsSession.createProducer(jmsDestination)
        msgProducer.send(message)
        createMsgCount.incrementAndGet()

        if (intyg.revoked) {
           Message revokeMessage = createMessage(jmsSession, intyg, true)
           msgProducer.send(revokeMessage)
           revokeMsgCount.incrementAndGet()
        }

        if (processedCount.incrementAndGet() % 1000 == 0) {
            printProgress();
        }
    }

    def createMessage(Session session, Intyg intyg, boolean revoked) {
        //if (appConf.debug.toBoolean()) println 'createMessage(' + session + ',' + intyg + ',' + revoked + ')'

        Message message = session.createTextMessage(intyg.contents)
        message.with {
            setStringProperty(JMS_PARAM_CERTIFICATE_ID, intyg.id)
            setStringProperty(JMS_PARAM_ACTION, (revoked) ? JMS_ACTION_REVOKED : JMS_ACTION_CREATED)
        }

        return message
    }

    def calcPercentDone() {
        if (appConf.debug.toBoolean()) println 'calcPercentDone()'

        Float total = (float) totalCount.get()
        Float done = (float) processedCount.get()
        Float percent = (done/total) * 100
        return percent.trunc(2)
    }

    def printProgress() {
        if (appConf.debug.toBoolean()) println 'printProgress()'

        println "- ${calcPercentDone()}% done at ${(int)((System.currentTimeMillis()-startTime) / 1000)} seconds: " +
        "Create msg: ${createMsgCount.get()} Revoke msg: ${revokeMsgCount.get()}"
    }

    def run() {
        if (appConf.debug.toBoolean()) println 'run()'

        println "Program starting..."
        startTime = System.currentTimeMillis()

        println("- Fetching list of intyg, might take a while...")
        def bootstrapSql = new Sql(dataSource)
        def intygIds = bootstrapSql.rows(getSqlStmt())
        totalCount.addAndGet(intygIds.size())
        bootstrapSql.close()

        println "- ${totalCount.get()} intyg found"
        println "- Starting putting intyg on queue: ${appConf.broker.queue}"

        for (intygIdRow in intygIds) {
            Intyg intyg = getIntyg(intygIdRow.ID)
            sendToQueue(intyg)
        }

        printProgress();
        println "Program done!"
    }

    def getSqlStmt() {
        if (appConf.debug.toBoolean()) println 'getSqlStmt()'

        def sqlStmt = appConf.sql.select
        if (!isNullOrEmpty(appConf.intygstyper) || !isNullOrEmpty(appConf.date.from)) {
            sqlStmt += ' WHERE '
            if (!isNullOrEmpty(appConf.intygstyper)) {
                sqlStmt += String.format(appConf.sql.where._1.clause, appConf.intygstyper)
                if (!isNullOrEmpty(appConf.date.from)) {
                    sqlStmt += ' AND '
                    sqlStmt += String.format(appConf.sql.where._2.clause, appConf.date.from, appConf.date.to)
                }
            } else {
                sqlStmt += String.format(appConf.sql.where._2.clause, appConf.date.from, appConf.date.to)
            }
        }

        if (appConf.sql.orderby) {
            sqlStmt += ' ORDER BY ' + appConf.sql.orderby
        }

        if (appConf.debug.toBoolean()) println 'sqlStmt = ' + sqlStmt
        return sqlStmt
    }

    def parseArguments(String[] args) {
        if (appConf.debug.toBoolean()) println 'parseArguments(' + args + ')'
        
        if (args) {
            Properties argsProperties = new Properties()
            args.each() {
                def index = it.indexOf('=', 0)
                if (index > -1) {
                    argsProperties.put(
                        shVariableToDotNotation(it.substring(0, index)), 
                        it.substring(index + 1).replaceAll(~/"/, ""))
                }
            }

            //println "argsProperties: " + argsProperties

            ConfigObject argsConf = new ConfigSlurper().parse(argsProperties)
            if (!argsConf.isEmpty()) {
                appConf.merge(argsConf)
            }
        }
        //println "appConf: " + appConf
        return appConf
    }

    def validateArguments() {
        if (appConf.debug.toBoolean()) println 'validateArguments()'

        assert isNullOrEmpty(appConf.db.url) == false
        assert isNullOrEmpty(appConf.db.driver) == false
        assert isNullOrEmpty(appConf.broker.url) == false
        assert isNullOrEmpty(appConf.broker.queue) == false

        if (!isNullOrEmpty(appConf.date.from)) {
            assert !isNullOrEmpty(appConf.date.to)
        }
    }

    /**
	 * Helper method to convert a camel case property name to a dot notated
	 * one. For example, myPropertyName would become my.property.name.
     * Property names that don't start with a lower case letter are assumed to not
     * be camel case and are returned as is.
	 * @param propertyName the name of the property to convert
	 * @return the converted property name
	 */
	def String camelCaseToDotNotation(String propertyName) {
		if ( !propertyName.charAt(0).isLowerCase() ) {
			return propertyName
		}

		StringBuilder sb = new StringBuilder();
		for ( char c : propertyName.getChars() ) {
			if (c.upperCase) {
				sb.append(".")
				sb.append(c.toLowerCase())
			} else {
				sb.append(c)
			}
		}
		return sb.toString()
	}

    /**
	 * Helper method to convert shell variable name to a dot notated
	 * one. The convention for a property is that all characters is
     * written in UPPERCASE and delimited with an underscore to separate
     * words.
     * 
     * For example, BROKER_URL would become broker.url.
     * 
     * Property names that are not all upper case are returned as is.
     * If property name includes a '_' then replace it with a '.' (dot).
     * 
	 * @param propertyName the name of the property to convert
	 * @return the converted property name
	 */
	def String shVariableToDotNotation(String propertyName) {
        if (!isUpperCase(propertyName)) {
            return propertyName
        }
        return propertyName.toLowerCase().replaceAll(~/_/, ".")
	}

    def boolean isUpperCase(String s) {
		for (char c : s.getChars()) {
            if (c.lowerCase) {
                return false
            }
        }
        return true
    }

    def boolean isNullOrEmpty(String str) { 
        if (!str?.trim()) {
            return true
        }
        return false
    }

    static void main(String[] args) {
        SkickaIntygTillIntygsstatistik app = new SkickaIntygTillIntygsstatistik('app.properties')
        app.parseArguments(args)
        app.validateArguments()
        app.setupDataSource()
        app.setupJMS()
        app.run()
        app.tearDownJMS()
    }

    class Intyg {
        String id;
        String contents;
        boolean revoked = false;
        public Intyg(String id) {
            this.id = id;
        }
    }
}


