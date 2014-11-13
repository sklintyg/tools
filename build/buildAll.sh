#!/bin/sh

export INTYG_HOME=`pwd`/../..

cd $INTYG_HOME/common-pom
mvn clean install
if [ $? != 0 ]; then exit 1; fi

cd $INTYG_HOME/common
mvn clean install
if [ $? != 0 ]; then exit 1; fi

cd $INTYG_HOME/tools
mvn clean install
if [ $? != 0 ]; then exit; fi

cd $INTYG_HOME/schemas
mvn clean install
if [ $? != 0 ]; then exit; fi

cd $INTYG_HOME/intygstyper
mvn clean install
if [ $? != 0 ]; then exit; fi

cd $INTYG_HOME/intygstjanst
mvn clean install
if [ $? != 0 ]; then exit; fi

cd $INTYG_HOME/minaintyg
mvn clean install
if [ $? != 0 ]; then exit; fi

cd $INTYG_HOME/webcert
mvn clean install
if [ $? != 0 ]; then exit; fi
