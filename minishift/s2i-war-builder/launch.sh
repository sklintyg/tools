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

echo "Running $APP_NAME on $JBOSS_IMAGE_NAME image, version $JBOSS_IMAGE_VERSION"

export CATALINA_OPTS="$CATALINA_OPTS $CATALINA_OPTS_APPEND"

exec $JWS_HOME/bin/catalina.sh run
