#!/bin/sh

export INTYG_HOME=`pwd`/../..

cd $INTYG_HOME
git clone https://github.com/sklintyg/common-pom.git
if [ $? != 0 ]; then exit 1; fi

git clone https://github.com/sklintyg/common.git
if [ $? != 0 ]; then exit 1; fi

git clone https://github.com/sklintyg/intygstyper.git
if [ $? != 0 ]; then exit; fi

git clone https://github.com/sklintyg/intygstjanst.git
if [ $? != 0 ]; then exit; fi

git clone https://github.com/sklintyg/minaintyg.git
if [ $? != 0 ]; then exit; fi

git clone https://github.com/sklintyg/schemas.git
if [ $? != 0 ]; then exit; fi

git clone https://github.com/sklintyg/webcert.git
if [ $? != 0 ]; then exit; fi