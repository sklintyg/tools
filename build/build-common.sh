#!/bin/sh

export INTYG_HOME=`pwd`/../..

cd $INTYG_HOME/common/pom
mvn install
cd $INTYG_HOME/common/support
mvn install
cd $INTYG_HOME/common/web
mvn install
cd $INTYG_HOME/common/util/logging-util
mvn install
cd $INTYG_HOME/common/util/integration-util
mvn install
