#!/bin/sh

export INTYG_HOME=`pwd`/../..

cd $INTYG_HOME/proxy
mvn install
