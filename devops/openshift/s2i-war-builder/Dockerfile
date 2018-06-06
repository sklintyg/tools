# Simple S2I builder based on openshift
FROM openshift/base-centos7

LABEL maintainer="inera.se"

# Install Java and Chrome etc.
ADD google-chrome.repo /etc/yum.repos.d/
RUN yum -y install java-1.8.0-openjdk-devel google-chrome-stable
RUN ln -s /etc/alternatives/google-chrome /usr/bin/chrome

# Env
ADD environment /etc/environment
RUN ln -snf /usr/share/zoneinfo/Europe/Stockholm /etc/localtime
RUN localedef -i en_US -f UTF-8 en_US.UTF-8
ENV LANG=en_US.UTF-8

# Add custom S2I scripts
ADD s2i/bin/ /usr/libexec/s2i/

# Replace launch.sh
ADD launch.sh /tmp/artifacts/
RUN mkdir -p /tmp/src /opt/app-root/src
RUN chown 1001 /tmp/artifacts /tmp/src
RUN chmod 777 /opt/app-root/src

# Run as user default
USER 1001
