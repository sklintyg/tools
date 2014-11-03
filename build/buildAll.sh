#!/bin/sh

./build-common.sh
if [ $? != 0 ]; then exit; fi

./build-tools.sh
if [ $? != 0 ]; then exit; fi

./build-schemas.sh
if [ $? != 0 ]; then exit; fi

./build-intygstyper.sh
if [ $? != 0 ]; then exit; fi

./build-intygstjanst.sh
if [ $? != 0 ]; then exit; fi

./build-minaintyg.sh
if [ $? != 0 ]; then exit; fi

./build-webcert.sh
if [ $? != 0 ]; then exit; fi
