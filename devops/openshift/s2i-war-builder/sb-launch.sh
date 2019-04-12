#!/bin/bash
#
# Spring Boot Launch Script

if [ "${SCRIPT_DEBUG}" = "true" ]; then
    set -x
    echo "Script debugging is enabled, allowing bash commands and their arguments to be printed as they are executed"
fi

CREDENTIALS=/opt/$APP_NAME/env/secret-env.sh
if [ -f $CREDENTIALS ]; then
    . $CREDENTIALS
fi

# if no REFDATA_URL exists get latest dev snapshot, otherwise use REFDATA_URL
# use xmllint to get latest version from maven-metadata
if [ -z $REFDATA_URL ]; then
    NEXUS_SNAPSHOT_URL="https://build-inera.nordicmedtest.se/nexus/repository/snapshots/se/inera/intyg/refdata/refdata/1.0-SNAPSHOT"
    REFDATA_VERSION=$(curl -Ls -m 20 ${NEXUS_SNAPSHOT_URL}/maven-metadata.xml | xmllint --xpath '//snapshotVersion/extension[.="jar"]/../value/text()' -)
    if [ $? != 0 ]; then
        echo "Error: unable to fetch refdata metadata: ${NEXUS_SNAPSHOT_URL}/maven-metadata.xml"
        exit 1
    fi
    REFDATA_URL="${NEXUS_SNAPSHOT_URL}/refdata-${REFDATA_VERSION}.jar"
fi

REFDATA_FILE=$(basename $REFDATA_URL)
REFDATA_JAR=sklintyg-${REFDATA_FILE%.*}.jar
curl -Ls -m 20 $REFDATA_URL > /tmp/$REFDATA_JAR
if [ $? != 0 ]; then
    echo "Error: unable to fetch refdata artifact: $REFDATA_URL"
    exit 1
fi

echo "With refdata from ${REFDATA_URL}"
JVM_OPTS="$JVM_OPTS -Dloader.path=/tmp/$REFDATA_JAR,WEB-INF/lib-provided,WEB-INF/lib,WEB-INF/classes"

# use legacy name for appending options
JVM_OPTS="$JVM_OPTS $CATALINA_OPTS_APPEND"

APP=$(ls /deployments/ | egrep '\.jar$|\.war$')
exec java $JVM_OPTS $JAVA_OPTS -jar /deployments/$APP


