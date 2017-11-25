FROM tomcat:7.0.82-jre8-alpine

# Remove default web app
RUN ["rm", "-fr", "/usr/local/tomcat/webapps/ROOT"]

ADD http://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.40/mysql-connector-java-5.1.40.jar lib/mysql-connector-java-5.1.40.jar
ADD http://repo1.maven.org/maven2/org/apache/activemq/activemq-client/5.13.0/activemq-client-5.13.0.jar lib/activemq-client-5.13.0.jar
ADD http://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.6.4/slf4j-api-1.6.4.jar lib/slf4j-api-1.6.4.jar
ADD http://repo1.maven.org/maven2/org/fusesource/hawtbuf/hawtbuf/1.9/hawtbuf-1.9.jar lib/hawtbuf-1.9.jar
ADD http://repo1.maven.org/maven2/org/apache/geronimo/specs/geronimo-jms_1.1_spec/1.1.1/geronimo-jms_1.1_spec-1.1.1.jar lib/geronimo-jms_1.1_spec-1.1.1.jar
ADD http://repo1.maven.org/maven2/org/apache/geronimo/specs/geronimo-j2ee-management_1.1_spec/1.0.1/geronimo-j2ee-management_1.1_spec-1.0.1.jar lib/geronimo-j2ee-management_1.1_spec-1.0.1.jar

COPY liquibase /liquibase
RUN rm -rf webapps/ROOT
RUN rm -rf webapps/ROOT.war
COPY intygstjanst-web-0-SNAPSHOT.war webapps/ROOT.war
COPY setenv.sh bin/setenv.sh
COPY start.sh start.sh
RUN chmod +x start.sh
COPY server.xml conf/server.xml
COPY context.xml conf/context.xml

CMD ["./start.sh"]
