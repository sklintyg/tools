#!/bin/sh

export INTYG_HOME=`pwd`/../..

git clone https://github.com/sklintyg/common.git
cd $INTYG_HOME
if [ $? != 0 ]; then exit 1; fi

git clone https://github.com/sklintyg/intygstyper.git
cd $INTYG_HOME
if [ $? != 0 ]; then exit; fi

git clone https://github.com/sklintyg/intygstjanst.git
cd $INTYG_HOME
if [ $? != 0 ]; then exit; fi

git clone https://github.com/sklintyg/minaintyg.git
cd $INTYG_HOME
if [ $? != 0 ]; then exit; fi

git clone https://github.com/sklintyg/schemas.git
cd $INTYG_HOME
if [ $? != 0 ]; then exit; fi

git clone https://github.com/sklintyg/webcert.git
cd $INTYG_HOME
if [ $? != 0 ]; then exit; fi