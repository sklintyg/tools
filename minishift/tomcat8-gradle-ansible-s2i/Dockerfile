FROM sarcouy/s2i-tomcat:8-jdk8-mvn3.5.0

USER root

# Install Ansible
RUN rpm -iUvh http://dl.fedoraproject.org/pub/epel/7/x86_64/Packages/e/epel-release-7-11.noarch.rpm
RUN yum -y update
RUN yum -y install ansible

# Install Gradle (actually, not really necessary since we're using the gradle wrapper anyway)
ENV GRADLE_VERSION 4.2
RUN curl -sL -0 https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -o /tmp/gradle-${GRADLE_VERSION}-bin.zip
RUN unzip /tmp/gradle-${GRADLE_VERSION}-bin.zip -d /usr/local/
RUN rm /tmp/gradle-${GRADLE_VERSION}-bin.zip
RUN mv /usr/local/gradle-${GRADLE_VERSION} /usr/local/gradle
RUN ln -sf /usr/local/gradle/bin/gradle /usr/local/bin/gradle

# Install some 3rd party default jars into tomcat. TODO do this using Ansible in the assemble step.
USER 1001
ADD http://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.40/mysql-connector-java-5.1.40.jar $CATALINA_HOME/lib/mysql-connector-java-5.1.40.jar
ADD http://repo1.maven.org/maven2/org/apache/activemq/activemq-client/5.13.0/activemq-client-5.13.0.jar $CATALINA_HOME/lib/activemq-client-5.13.0.jar
ADD http://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.6.4/slf4j-api-1.6.4.jar $CATALINA_HOME/lib/slf4j-api-1.6.4.jar
ADD http://repo1.maven.org/maven2/org/fusesource/hawtbuf/hawtbuf/1.9/hawtbuf-1.9.jar $CATALINA_HOME/lib/hawtbuf-1.9.jar
ADD http://repo1.maven.org/maven2/org/apache/geronimo/specs/geronimo-jms_1.1_spec/1.1.1/geronimo-jms_1.1_spec-1.1.1.jar $CATALINA_HOME/lib/geronimo-jms_1.1_spec-1.1.1.jar
ADD http://repo1.maven.org/maven2/org/apache/geronimo/specs/geronimo-j2ee-management_1.1_spec/1.0.1/geronimo-j2ee-management_1.1_spec-1.0.1.jar $CATALINA_HOME/lib/geronimo-j2ee-management_1.1_spec-1.0.1.jar

USER root

# Fix permissions for where the source is checked out by OpenShift S2I
RUN chmod -R 775 /opt/app-root/src
RUN chown -R 1001 /opt/app-root/src

# Copy our own S2I scripts to the image's path, overwriting some of the scripts from the base image.
COPY ./.s2i/bin/ $STI_SCRIPTS_PATH

# Fix persmissions.
RUN chmod -R 775 $CATALINA_HOME/lib
RUN chown -R 1001 $CATALINA_HOME/lib
RUN chmod -R 775 $STI_SCRIPTS_PATH
RUN chown -R 1001 $STI_SCRIPTS_PATH

USER 1001
