#!/bin/sh

export INTYG_HOME=`pwd`/../..

cd $INTYG_HOME/common-pom
git checkout develop
if [ $? != 0 ]; then exit 1; fi

cd $INTYG_HOME/common
git checkout develop
if [ $? != 0 ]; then exit 1; fi

cd $INTYG_HOME/intygstyper
git checkout develop
if [ $? != 0 ]; then exit; fi

cd $INTYG_HOME/intygstjanst
git checkout develop
if [ $? != 0 ]; then exit; fi

cd $INTYG_HOME/minaintyg
git checkout develop
if [ $? != 0 ]; then exit; fi
	
cd $INTYG_HOME/schemas
git checkout develop
if [ $? != 0 ]; then exit; fi

cd $INTYG_HOME/tools
git checkout develop
if [ $? != 0 ]; then exit; fi
		
cd $INTYG_HOME/webcert
git checkout develop
if [ $? != 0 ]; then exit; fi
