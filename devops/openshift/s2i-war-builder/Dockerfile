# Simple S2I builder based on openshift
FROM openshift/base-centos7

LABEL maintainer="inera.se"

# Install Java
RUN yum -y install java-1.8.0-openjdk-devel

# Add custom S2I scripts
ADD s2i/bin/ /usr/libexec/s2i/

# Replace launch.sh
ADD launch.sh /tmp/artifacts/
RUN mkdir -p /tmp/src /opt/app-root/src
RUN chown 1001 /tmp/artifacts /tmp/src
RUN chmod 777 /opt/app-root/src

# Run as user default
USER 1001
