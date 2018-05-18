#!/bin/sh
git clone -v $GIT_URL repo

# Execute
repo/gradlew assemble restAssuredTest -DbaseUrl=$TARGET_URL -DbuildVersion=$BUILD_VERSION -DcommonVersion=$COMMON_VERSION -DinfraVersion=$INFRA_VERSION

# Copy test results to persistent volume mount
cp -r repo/web/build/reports/tests/restAssuredTest/* /tmp/reports/$JOB_NAME/$BUILD_VERSION/

# Notify back to pipeline
curl -X POST -k -d "DONE" $CALLBACK_URL
