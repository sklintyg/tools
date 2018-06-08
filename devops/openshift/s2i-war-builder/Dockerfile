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

# Default project UID  
ARG UID=1001
ENV RUN_UID=${UID}

# Add custom S2I scripts
ADD s2i/bin/ /usr/libexec/s2i/

# Replace launch.sh
ADD launch.sh /tmp/artifacts/

# S2I uses default user, but when referenced by a testrunner pod another user is active (according to project settings)
# Setup permissions to enable an unknown user to access different locations used by gradle, npm, bower, chrome, ...
RUN mkdir -p /tmp/src /opt/app-root/src /opt/app-root/src/.npm /opt/app-root/src/.local/share
RUN chown ${UID} /tmp/artifacts /tmp/src /opt/app-root/src /opt/app-root/src/.npm /opt/app-root/src/.local /opt/app-root/src/.local/share
RUN chmod 777 /tmp/artifacts /tmp/src /opt/app-root/src /opt/app-root/src/.npm /opt/app-root/src/.local /opt/app-root/src/.local/share

# Run as user default (for S2I, otheriwse another low-prio)
USER ${UID}
