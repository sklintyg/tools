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

RESOURCES=/opt/$APP_NAME/env/resources.zip
if [ -f $RESOURCES ]; then
    (mkdir -p /tmp/resources; cd /tmp/resources; unzip $RESOURCES)    
else
    REFDATA_NEXUS="https://build-inera.nordicmedtest.se/nexus/repository/public/se/inera/intyg/refdata/refdata/maven-metadata.xml"
    if [ -z $REFDATA_URL ]; then
        REFDATA_VERSION=$(curl -s $REFDATA_NEXUS | grep '<latest>.*</latest>' | sed 's/.*<latest>\(.*\)<\/latest>.*/\1/g')
        REFDATA_URL="$(dirname $REFDATA_NEXUS)/$REFDATA_VERSION/refdata-${REFDATA_VERSION}.jar"
    fi

    REFDATA_FILE=$(basename $REFDATA_URL)
    REFDATA_JAR=sklintyg-${REFDATA_FILE%.*}.jar
    curl -Ls $REFDATA_URL > $REFDATA_JAR
    if [ $? != 0 ]; then
        echo "Error: unable to fetch refdata: $REFDATA_URL"
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


