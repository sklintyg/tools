#!/bin/sh
git clone -v $GIT_URL repo
ls -lah repo

# Execute
cd repo && ./gradlew assemble restAssuredTest -DbaseUrl=$TARGET_URL -DbuildVersion=$BUILD_VERSION -DcommonVersion=$COMMON_VERSION -DinfraVersion=$INFRA_VERSION

# Copy test results to persistent volume mount
mkdir -p /mnt/reports/$JOB_NAME/$BUILD_VERSION
cp -R /opt/app-root/repo/web/build/reports/tests/restAssuredTest/* /mnt/reports/$JOB_NAME/$BUILD_VERSION/.

# Notify back to pipeline
if [ $? -eq 0 ]; then
  curl -X POST -k -d "SUCCESS" $CALLBACK_URL
else
  curl -X POST -k -d "FAILED" $CALLBACK_URL
fi
