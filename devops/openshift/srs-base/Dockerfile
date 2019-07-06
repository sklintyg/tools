FROM docker-registry.default.svc:5000/dintyg/springboot-base:latest

USER root
RUN yum -y erase java-11-openjdk-devel
ENV R_HOME=/usr/lib64/R
ENV JVM_OPTS="-Djava.library.path=$R_HOME/library/rJava/jri"

RUN yum -y install epel-release
RUN yum -y install R
RUN yum -y install java-1.8.0-openjdk-devel
RUN R -e 'install.packages("rJava",,"http://www.rforge.net/")'
RUN R -e 'install.packages("pch",,"https://cloud.r-project.org/")'

USER nobody
