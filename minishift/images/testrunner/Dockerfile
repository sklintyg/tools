FROM nimmis/java-centos:openjdk-8-jdk

USER root
RUN yum -y install git curl bzip2 && yum clean all -y

### Setup user for build execution and application runtime
ENV APP_ROOT=/opt
ENV PATH=${APP_ROOT}/app-root/bin:${PATH} HOME=${APP_ROOT} JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk 
ENV JAVA_TOOL_OPTIONS -Dfile.encoding=UTF8
COPY bin/ ${APP_ROOT}/app-root/bin/
RUN chmod -R u+x ${APP_ROOT}/app-root/bin && \
    chgrp -R 0 ${APP_ROOT} && chown -R 1001:0 ${APP_ROOT} && \
    chmod -R g=u ${APP_ROOT} /etc/passwd

### Containers should NOT run as root as a good practice
USER 1001
WORKDIR ${APP_ROOT}/app-root

### user name recognition at runtime w/ an arbitrary uid - for OpenShift deployments
ENTRYPOINT [ "uid_entrypoint" ]
CMD run.sh
