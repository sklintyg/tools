#!/bin/sh
export INTYG_HOME=`pwd`/../..

echo '--- common-pom ---'
cd $INTYG_HOME/common-pom
git status
if [ $? != 0 ]; then exit 1; fi

echo '--- common ---'
cd $INTYG_HOME/common
git status
if [ $? != 0 ]; then exit 1; fi

echo '--- tools ---'
cd $INTYG_HOME/tools
git status
if [ $? != 0 ]; then exit; fi

echo '--- schemas ---'
cd $INTYG_HOME/schemas
git status
if [ $? != 0 ]; then exit; fi

echo '--- intygstyper ---'
cd $INTYG_HOME/intygstyper
git status
if [ $? != 0 ]; then exit; fi

echo '--- intygstjänst ---'
cd $INTYG_HOME/intygstjanst
git status
if [ $? != 0 ]; then exit; fi

echo '--- mina intyg ---'
cd $INTYG_HOME/minaintyg
git status
if [ $? != 0 ]; then exit; fi

echo '--- webcert ---'
cd $INTYG_HOME/webcert
git status
if [ $? != 0 ]; then exit; fi
