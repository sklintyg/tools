#!/bin/sh

export INTYG_HOME=`pwd`/../..

cd $INTYG_HOME/tools/anonymisering
mvn clean install
if [ $? != 0 ]; then exit 1; fi

cd $INTYG_HOME/tools/liquibase-runner
mvn clean install
if [ $? != 0 ]; then exit 1; fi
