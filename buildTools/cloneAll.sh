#!/bin/sh

export INTYG_HOME=`pwd`/../..
cd $INTYG_HOME

echo '--- common-pom ---'
git clone https://github.com/sklintyg/common-pom.git
if [ $? != 0 ]; then exit 1; fi

echo '--- common ---'
git clone https://github.com/sklintyg/common.git
if [ $? != 0 ]; then exit 1; fi

echo '--- intygstyper ---'
git clone https://github.com/sklintyg/intygstyper.git
if [ $? != 0 ]; then exit; fi

echo '--- intygstjänst ---'
git clone https://github.com/sklintyg/intygstjanst.git
if [ $? != 0 ]; then exit; fi

echo '--- mina intyg ---'
git clone https://github.com/sklintyg/minaintyg.git
if [ $? != 0 ]; then exit; fi

echo '--- schemas ---'
git clone https://github.com/sklintyg/schemas.git
if [ $? != 0 ]; then exit; fi

echo '--- tools ---'
git clone https://github.com/sklintyg/tools.git
if [ $? != 0 ]; then exit; fi
    
echo '--- webcert ---'
git clone https://github.com/sklintyg/webcert.git
if [ $? != 0 ]; then exit; fi