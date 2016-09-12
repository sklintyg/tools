#!/bin/sh

export INTYG_HOME=`pwd`/../..

echo '--- common-pom ---'
cd $INTYG_HOME/common-pom
git pull --rebase origin develop
if [ $? != 0 ]; then exit 1; fi

echo '--- common ---'
cd $INTYG_HOME/common
git pull --rebase origin develop
if [ $? != 0 ]; then exit 1; fi

echo '--- tools ---'
cd $INTYG_HOME/tools
git pull --rebase origin develop
if [ $? != 0 ]; then exit; fi

echo '--- schemas ---'
cd $INTYG_HOME/schemas
git pull --rebase origin develop
if [ $? != 0 ]; then exit; fi

echo '--- intygstyper ---'
cd $INTYG_HOME/intygstyper
git pull --rebase origin develop
if [ $? != 0 ]; then exit; fi

echo '--- intygstjänst ---'
cd $INTYG_HOME/intygstjanst
git pull --rebase origin develop
if [ $? != 0 ]; then exit; fi

echo '--- mina intyg ---'
cd $INTYG_HOME/minaintyg
git pull --rebase origin develop
if [ $? != 0 ]; then exit; fi

echo '--- webcert ---'
cd $INTYG_HOME/webcert
git pull --rebase origin develop
if [ $? != 0 ]; then exit; fi
