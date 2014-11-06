#!/bin/sh

export INTYG_HOME=`pwd`/../..

$INTYG_HOME/common/build/build-common.sh
if [ $? != 0 ]; then exit; fi

$INTYG_HOME/tools/build/build-tools.sh
if [ $? != 0 ]; then exit; fi

$INTYG_HOME/schemas/build/build-schemas.sh
if [ $? != 0 ]; then exit; fi

$INTYG_HOME/intygstyper/build/build-intygstyper.sh
if [ $? != 0 ]; then exit; fi

$INTYG_HOME/intygstjanst/build/build-intygstjanst.sh
if [ $? != 0 ]; then exit; fi

$INTYG_HOME/minaintyg/build/build-minaintyg.sh
if [ $? != 0 ]; then exit; fi

$INTYG_HOME/webcert/build/build-webcert.sh
if [ $? != 0 ]; then exit; fi
