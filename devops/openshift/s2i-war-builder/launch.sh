#!/bin/bash
# Overrides the default startup launcher (with this simplified)

if [ "${SCRIPT_DEBUG}" = "true" ]; then
    set -x
    echo "Script debugging is enabled, allowing bash commands and their arguments to be printed as they are executed"
fi

CREDENTIALS=/opt/$APP_NAME/env/secret-env.sh
if [ -f $CREDENTIALS ]; then
    . $CREDENTIALS
fi

# Use resources.zip if exists
# if no REFDATA_URL exists get latest dev snapshot, otherwise use REFDATA_URL
# use xmllint to get latest version from maven-metadata
RESOURCES=/opt/$APP_NAME/env/resources.zip
if [ -f $RESOURCES ]; then
    (mkdir -p /tmp/resources; cd /tmp/resources; unzip $RESOURCES)    
else
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
    curl -Ls -m 20 $REFDATA_URL > $REFDATA_JAR
    if [ $? != 0 ]; then
        echo "Error: unable to fetch refdata artifact: $REFDATA_URL"
        exit 1
    fi

    mv $REFDATA_JAR $JWS_HOME/lib/
    if [ $? != 0 ]; then
        echo "Error: unable to provision refdata: $REFDATA_JAR"
        exit 1
    fi
fi

echo "Running $APP_NAME on $JBOSS_IMAGE_NAME image, version $JBOSS_IMAGE_VERSION"
echo "With refdata from ${REFDATA_URL:-resources.zip}"

export CATALINA_OPTS="$CATALINA_OPTS $CATALINA_OPTS_APPEND"

exec $JWS_HOME/bin/catalina.sh run


