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
class SkickaIntygSentTillIntygsstatistik {

    static final JMS_ACTION_SENT = "sent"
    static final JMS_PARAM_CERTIFICATE_ID = "certificate-id"
    static final JMS_PARAM_CERTIFICATE_RECIPIENT = "certificate-recipient"
    static final JMS_PARAM_ACTION = "action"

    final AtomicInteger totalCount = new AtomicInteger(0)
    final AtomicInteger processedCount = new AtomicInteger(0)
    final AtomicInteger createMsgCount = new AtomicInteger(0)

    long startTime = 0;

    final lock = new Object()
 
    ConfigObject appConf = null

    BasicDataSource dataSource = null
    Connection jmsConnection = null
    Session jmsSession = null
    Destination jmsDestination = null

    public SkickaIntygSentTillIntygsstatistik() {
        this(null)
    }

    public SkickaIntygSentTillIntygsstatistik(String fileName) {
        Properties appProperties = new Properties()

        if (fileName) {
            this.getClass().getResource('/' + fileName).withInputStream {
                appProperties.load(it)
            }
        }

        // Init application's default configuration
        appConf = new ConfigSlurper().parse(appProperties)
        appConf.debug = false
    }

    static void main(String[] args) {
        SkickaIntygSentTillIntygsstatistik app = new SkickaIntygSentTillIntygsstatistik('app.properties')
        app.parseArguments(args)
        app.validateArguments()
        app.setupDataSource()
        app.setupJMS()
        app.run()
        app.tearDownJMS()
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

    def run() {
        println "Program starting..."
        startTime = System.currentTimeMillis()

        println("- Fetching list of intyg, might take a while...")
        
        def bootstrapSql = Sql.newInstance(dataSource)
        def intygIds = bootstrapSql.rows(getSqlStmt())
        bootstrapSql.close()
        totalCount.addAndGet(intygIds.size())

        println "- ${totalCount.get()} intyg found"
        println "- Starting putting intyg on queue: ${appConf.broker.queue}"

        def sql = Sql.newInstance(dataSource)

        intygIds.each {            
            sendToQueue(it.intygId, it.reciverId)
        }

        sql.close()

        printProgress();
        println "Program done!"
    }

    def sendToQueue(String intygId, String intygRecipient) {
        Message message = createMessage(jmsSession, intygId, intygRecipient)
        MessageProducer msgProducer = jmsSession.createProducer(jmsDestination)
        msgProducer.send(message)
        createMsgCount.incrementAndGet()

        if (processedCount.incrementAndGet() % 1000 == 0) {
            printProgress();
        }
    }

    def createMessage(Session session, String intygId, String intygRecipient) {
        Message message = session.createTextMessage(null)
        message.with {
            setStringProperty(JMS_PARAM_CERTIFICATE_ID, intygId)
            setStringProperty(JMS_PARAM_CERTIFICATE_RECIPIENT, intygRecipient)
            setStringProperty(JMS_PARAM_ACTION, JMS_ACTION_SENT)
        }

        if (appConf.debug.toBoolean()) {
            println "JMS_PARAM_CERTIFICATE_ID ${intygId}"
            println "JMS_PARAM_CERTIFICATE_RECIPIENT ${intygRecipient}"
        }

        return message
    }

    def printProgress() {
        println "- ${calcPercentDone()}% done at ${(int)((System.currentTimeMillis()-startTime) / 1000)} seconds: " +
        "Create msg: ${createMsgCount.get()} "
    }

    def calcPercentDone() {
        Float total = (float) totalCount.get()
        Float done = (float) processedCount.get()
        Float percent = (done/total) * 100
        return percent.trunc(2)
    }

    def getSqlStmt() {
        String sqlStmt = "SELECT " +
                        "     cs.CERTIFICATE_ID as intygId, cs.TARGET as reciverId " +
                        " FROM " +
                        "     CERTIFICATE_STATE as cs " +
                        " WHERE " +
                        "     cs.STATE = 'SENT' "
            
        if (!isNullOrEmpty(appConf.date.from)) {
            sqlStmt += String.format("AND cs.TIMESTAMP BETWEEN '%s' and '%s'", appConf.date.from, appConf.date.to)
        }        

        return sqlStmt
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
}


