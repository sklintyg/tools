#!/bin/bash
cd /liquibase
bin/intygstjanst-liquibase-runner --url=jdbc:mysql://mysql:3306/intyg?useCompression=true --username=intyg --password=intyg update;
cd /usr/local/tomcat
catalina.sh run
