FROM registry.access.redhat.com/jboss-webserver-5/webserver50-tomcat9-openshift:1.2
LABEL maintainer="inera.se"

RUN rm -rf $JWS_HOME/webapps/* \
    $JWS_HOME/lib/mysql-connector-java.jar $JWS_HOME/lib/mongo.jar \
    $JWS_HOME/lib/jolokia.jar $JWS_HOME/lib/postgresql-jdbc.jar

ADD http://repo1.maven.org/maven2/javax/servlet/jsp/jstl/javax.servlet.jsp.jstl-api/1.2.1/javax.servlet.jsp.jstl-api-1.2.1.jar $JWS_HOME/lib
ADD http://repo1.maven.org/maven2/org/glassfish/web/javax.servlet.jsp.jstl/1.2.1/javax.servlet.jsp.jstl-1.2.1.jar  $JWS_HOME/lib  

USER root
RUN ln -snf /usr/share/zoneinfo/Europe/Stockholm /etc/localtime
RUN localedef -i en_US -f UTF-8 en_US.UTF-8
RUN chown jboss $JWS_HOME/lib/*
RUN chmod 644 $JWS_HOME/lib/*
USER jboss

ADD launch.sh $JWS_HOME/bin/
ADD conf/server.xml $JWS_HOME/conf/
