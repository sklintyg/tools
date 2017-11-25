#!/bin/bash
cd /liquibase
bin/intygstjanst-liquibase-runner --url=jdbc:mysql://mysql:3306/intyg?useCompression=true --username=$INTYG_DB_USERNAME --password=$INTYG_DB_PASSWORD update;
cd /usr/local/tomcat
catalina.sh run
