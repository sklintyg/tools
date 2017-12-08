FROM registry.access.redhat.com/jboss-webserver-3/webserver30-tomcat8-openshift
# Note: $JWS_HOME is /opt/webserver
USER root
ENV GRADLE_VERSION 4.2
RUN curl -sL -0 https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -o /tmp/gradle-${GRADLE_VERSION}-bin.zip
RUN unzip /tmp/gradle-${GRADLE_VERSION}-bin.zip -d /usr/local/
RUN rm /tmp/gradle-${GRADLE_VERSION}-bin.zip
RUN mv /usr/local/gradle-${GRADLE_VERSION} /usr/local/gradle
RUN ln -sf /usr/local/gradle/bin/gradle /usr/local/bin/gradle

USER 185
# ADD http://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.40/mysql-connector-java-5.1.40.jar $JWS_HOME/lib
ADD http://repo1.maven.org/maven2/org/apache/activemq/activemq-client/5.13.0/activemq-client-5.13.0.jar $JWS_HOME/lib
ADD http://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.6.4/slf4j-api-1.6.4.jar $JWS_HOME/lib
ADD http://repo1.maven.org/maven2/org/fusesource/hawtbuf/hawtbuf/1.9/hawtbuf-1.9.jar $JWS_HOME/lib
ADD http://repo1.maven.org/maven2/org/apache/geronimo/specs/geronimo-jms_1.1_spec/1.1.1/geronimo-jms_1.1_spec-1.1.1.jar $JWS_HOME/lib
ADD http://repo1.maven.org/maven2/org/apache/geronimo/specs/geronimo-j2ee-management_1.1_spec/1.0.1/geronimo-j2ee-management_1.1_spec-1.0.1.jar $JWS_HOME/lib

USER root
RUN chmod -R 775 $JWS_HOME/lib
RUN chown -R 185 $JWS_HOME/lib
# RUN echo $STI_SCRIPTS_PATH

COPY ./.s2i/bin/ /usr/local/s2i

RUN chmod -R 775 $JWS_HOME/lib
RUN chown -R 185 $JWS_HOME/lib
RUN chmod -R 775 /usr/local/s2i
RUN chown -R 185 /usr/local/s2i

USER 185
