#!/bin/sh
git clone $GIT_URL repo

# Execute
#repo/gradlew assemble restAssuredTest -DbaseUrl=$TARGET_URL -DbuildVersion=$BUILD_VERSION -DcommonVersion=$COMMON_VERSION -DinfraVersion=$INFRA_VERSION

# Cleanup
rm -rf repo