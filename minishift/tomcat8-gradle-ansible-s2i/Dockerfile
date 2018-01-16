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

# Fix permissions for where the source is checked out by OpenShift S2I
RUN chmod -R 775 /opt/app-root/src
RUN chown -R 1001 /opt/app-root/src

# Copy our own S2I scripts to the image's path, overwriting some of the scripts from the base image.
COPY ./.s2i/bin/ $STI_SCRIPTS_PATH

# Fix persmissions. Needed for both copying of resources and later deleting unwanted stuff from the final container image.
RUN chmod -R 775 $CATALINA_HOME/lib
RUN chown -R 1001 $CATALINA_HOME/lib
RUN chmod -R 775 $STI_SCRIPTS_PATH
RUN chown -R 1001 $STI_SCRIPTS_PATH
RUN chmod -R 775 /usr/local/gradle
RUN chown -R 1001 /usr/local/gradle
RUN chgrp -R 1001 /usr/local/gradle
RUN chmod -R 775 /usr/local/apache-maven-3.5.0
RUN chown -R 1001 /usr/local/apache-maven-3.5.0
RUN chgrp -R 1001 /usr/local/apache-maven-3.5.0

USER 1001
