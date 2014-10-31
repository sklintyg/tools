#!/bin/sh

export INTYG_HOME=`pwd`/../..

cd $INTYG_HOME/minaintyg
mvn install
