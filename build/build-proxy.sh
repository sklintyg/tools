#!/bin/sh

export INTYG_HOME=`pwd`/../..

cd $INTYG_HOME/proxy
mvn clean install
if [ $? != 0 ]; then exit 1; fi
