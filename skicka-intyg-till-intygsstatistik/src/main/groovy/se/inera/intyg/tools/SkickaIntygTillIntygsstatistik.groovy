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

package se.inera.intyg.tools;

import groovy.sql.Sql

import java.util.concurrent.atomic.AtomicInteger

import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.activemq.network.jms.JmsTopicConnector;

import javax.jms.Connection
import javax.jms.Message
import javax.jms.MessageProducer
import javax.jms.Session
import javax.jms.Destination

import org.apache.commons.dbcp2.BasicDataSource

/**
 * Script for putting all intyg found in Intygtjänsten on a specific JMS queue.
 * 
 * For intyg that has been revoked, two messages will be sent. One with action 'created'
 * and one with action 'revoked'.
 * 
 * @author npet
 *
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
        Properties appProperties = new Properties()

        new File(getClass().getResource('/app.properties').toURI()).withInputStream { stream ->
            appProperties.load(stream)
        }

        // Init application's default configuration
        appConf = new ConfigSlurper().parse(appProperties)
        appConf.debug = false
    }

    def setupDataSource() {
        if (appConf.debug == true) println 'setupDataSource()'

        dataSource = new BasicDataSource(driverClassName: appConf.dataSource.driver, url: appConf.dataSource.url,
        username: appConf.dataSource.username, password: appConf.dataSource.password,
        initialSize: 1, maxTotal: 1)
    }
    
    def setupJMS() {
        if (appConf.debug == true) println 'setupJMS()'

        jmsConnection = new ActiveMQConnectionFactory(brokerURL: appConf.broker.url).createConnection()
        jmsSession = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE)
        jmsDestination = jmsSession.createQueue(appConf.broker.queue)
    }
    
    def tearDownJMS() {
        if (appConf.debug == true) println 'tearDownJMS()'

        if (jmsSession) {
            jmsSession.close()
        }        
        if (jmsConnection) {
            jmsConnection.close()
        }
    }

    def getIntyg(def intygId) {
        if (appConf.debug == true) println 'getIntyg(' + intygId + ')'

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
        if (appConf.debug == true) println 'sendToQueue(' + intyg + ')'

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
        if (appConf.debug == true) println 'createMessage(' + session + ',' + intyg + ',' + revoked + ')'

        Message message = session.createTextMessage(intyg.contents)
        message.with {
            setStringProperty(JMS_PARAM_CERTIFICATE_ID, intyg.id)
            setStringProperty(JMS_PARAM_ACTION, (revoked) ? JMS_ACTION_REVOKED : JMS_ACTION_CREATED)
        }
        
        return message
    }
    
    def calcPercentDone() {
        if (appConf.debug == true) println 'calcPercentDone()'

        Float total = (float) totalCount.get()
        Float done = (float) processedCount.get()
        Float percent = (done/total) * 100
        return percent.trunc(2)
    }
    
    def printProgress() {
        if (appConf.debug == true) println 'printProgress()'

        println "- ${calcPercentDone()}% done at ${(int)((System.currentTimeMillis()-startTime) / 1000)} seconds: " +
        "Create msg: ${createMsgCount.get()} Revoke msg: ${revokeMsgCount.get()}"
    }

    def run() {
        if (appConf.debug == true) println 'run()'

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
        if (appConf.debug == true) println 'getSqlStmt()'

        def sqlStmt = appConf.sql.select
        if (appConf.sql.where._1.value.intygstyper || appConf.sql.where._1.value.date.from) {
            sqlStmt += ' WHERE '
            if (appConf.sql.where._1.value.intygstyper) {
                sqlStmt += String.format(appConf.sql.where._1.clause, appConf.sql.where._1.value.intygstyper)
            }
            if (appConf.sql.where._2.value.date.from) {
                if (appConf.sql.where._1.value.intygstyper) {
                    sqlStmt += ' AND '
                }
                sqlStmt += String.format(appConf.sql.where._2.clause, appConf.sql.where._2.value.date.from, appConf.sql.where._2.value.date.to)
            }
        }

        if (appConf.sql.orderby) {
            sqlStmt += ' ORDER BY ' + appConf.sql.orderby
        }

        return sqlStmt
    }

    def parseArguments(String[] args) {
        if (appConf.debug == true) println 'parseArguments(' + args + ')'

        if (args) {
            Properties argsProperties = new Properties()            
            args.each() {
                def index = it.indexOf('=', 0)
                if (index > -1) {
                    argsProperties.put(it.substring(0, index), it.substring(index + 1))
                }
            }            
        
            ConfigObject argsConf = new ConfigSlurper().parse(argsProperties)            
            if (!argsConf.isEmpty()) {
                appConf.merge(argsConf)
            }
        }    
    }
    
    def validateArguments() {
        if (appConf.debug == true) println 'validateArguments()'

        // This is a tight coupling between build.gradle and this class
        if (appConf.sql.where._2.value.date.from) {
            assert appConf.sql.where._2.value.date.to
        }
    }
    
    static void main(String[] args) {
        SkickaIntygTillIntygsstatistik app = new SkickaIntygTillIntygsstatistik()
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
