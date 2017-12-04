FROM tomcat:7.0.82-jre8-alpine

# Add lib files to tomcat. This should be done in an intermediate build (e.g. use multi-stage builds)
ADD http://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.40/mysql-connector-java-5.1.40.jar lib/mysql-connector-java-5.1.40.jar
ADD http://repo1.maven.org/maven2/org/apache/activemq/activemq-client/5.13.0/activemq-client-5.13.0.jar lib/activemq-client-5.13.0.jar
ADD http://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.6.4/slf4j-api-1.6.4.jar lib/slf4j-api-1.6.4.jar
ADD http://repo1.maven.org/maven2/org/fusesource/hawtbuf/hawtbuf/1.9/hawtbuf-1.9.jar lib/hawtbuf-1.9.jar
ADD http://repo1.maven.org/maven2/org/apache/geronimo/specs/geronimo-jms_1.1_spec/1.1.1/geronimo-jms_1.1_spec-1.1.1.jar lib/geronimo-jms_1.1_spec-1.1.1.jar
ADD http://repo1.maven.org/maven2/org/apache/geronimo/specs/geronimo-j2ee-management_1.1_spec/1.0.1/geronimo-j2ee-management_1.1_spec-1.0.1.jar lib/geronimo-j2ee-management_1.1_spec-1.0.1.jar

# Copy liquibase (TODO do this directly from Jenkins)
COPY liquibase /liquibase

# Remove default web app
RUN rm -rf webapps/ROOT
RUN rm -rf webapps/ROOT.war

# Copy artifact. Do this from Jenkins?
COPY intygstjanst-web-0-SNAPSHOT.war webapps/ROOT.war

# Copy environment variable script and startup script.
COPY setenv.sh bin/setenv.sh
COPY start.sh start.sh
RUN chmod +x start.sh

# Copy modified tomcat/conf configuration
COPY server.xml conf/server.xml
COPY context.xml conf/context.xml

# Execute the start script, will first run liquibase, then start the Tomcat.
CMD ["./start.sh"]
