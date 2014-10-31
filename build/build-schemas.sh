#!/bin/sh

export INTYG_HOME=`pwd`/../..

cd $INTYG_HOME/schemas
mvn install
