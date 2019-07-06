# Simple S2I builder based on openshift
FROM openshift/base-centos7

LABEL maintainer="inera.se"

# Install Java and Chrome etc.
ADD google-chrome.repo /etc/yum.repos.d/
RUN yum -y update
RUN yum -y install java-1.8.0-openjdk-devel google-chrome-stable xorg-x11-server-Xvfb gtk2 GConf2 libnotify
RUN ln -s /etc/alternatives/google-chrome /usr/bin/chrome

# Env (localtime)
ADD environment /etc/environment
RUN ln -snf /usr/share/zoneinfo/Europe/Stockholm /etc/localtime
RUN localedef -i en_US -f UTF-8 en_US.UTF-8
ENV LANG=en_US.UTF-8

# Default project UID  
ARG X_UID=1001
ENV X_UID=${X_UID}

# Add custom S2I scripts
ADD s2i/bin/ /usr/libexec/s2i/

# S2I runs as user default, but when invoked by the testrunner pod another user (uid) is active (depends on openshift project settings)
# Setup permissions to enable an unknown user to access different locations used by gradle, npm, bower, chrome, ...
ARG X_DIRS="/tmp/artifacts /tmp/src /tmp/nohome"
RUN mkdir -p ${X_DIRS} && chown ${X_UID} ${X_DIRS} && chmod 777 ${X_DIRS}

# Run as user default for S2I, otheriwse another unspecified
USER ${X_UID}
