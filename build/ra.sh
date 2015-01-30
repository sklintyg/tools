#!/bin/sh

export INTYG_HOME=`pwd`/../..

cd $INTYG_HOME/common-pom
git pull --rebase origin develop
if [ $? != 0 ]; then exit 1; fi

cd $INTYG_HOME/common
git pull --rebase origin develop
if [ $? != 0 ]; then exit 1; fi

cd $INTYG_HOME/tools
git pull --rebase origin develop
if [ $? != 0 ]; then exit; fi

cd $INTYG_HOME/schemas
git pull --rebase origin develop
if [ $? != 0 ]; then exit; fi

cd $INTYG_HOME/intygstyper
git pull --rebase origin develop
if [ $? != 0 ]; then exit; fi

cd $INTYG_HOME/intygstjanst
git pull --rebase origin develop
if [ $? != 0 ]; then exit; fi

cd $INTYG_HOME/minaintyg
git pull --rebase origin develop
if [ $? != 0 ]; then exit; fi

cd $INTYG_HOME/webcert
git pull --rebase origin develop
if [ $? != 0 ]; then exit; fi
