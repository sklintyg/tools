#!/bin/sh

export INTYG_HOME=`pwd`/../..

cd $INTYG_HOME/tools/anonymisering
mvn install
cd $INTYG_HOME/tools/liquibase-runner
mvn install
