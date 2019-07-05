FROM centos:7
LABEL maintainer="inera.se"

ENV JVM_OPTS="-Dfile.encoding=UTF-8"
EXPOSE 8080
EXPOSE 8081

USER root
RUN yum -y install java-11-openjdk-devel
RUN ln -snf /usr/share/zoneinfo/Europe/Stockholm /etc/localtime
RUN localedef -i en_US -f UTF-8 en_US.UTF-8
RUN mkdir -p /deployments /opt/app/bin
RUN chmod 777 /deployments
ADD launch.sh /opt/app/bin/

USER nobody
CMD ["/opt/app/bin/launch.sh"]
