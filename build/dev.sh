#!/bin/sh

export INTYG_HOME=`pwd`/../..

cd common-pom
git checkout develop
cd $INTYG_HOME
if [ $? != 0 ]; then exit 1; fi

cd common
git checkout develop
cd $INTYG_HOME
if [ $? != 0 ]; then exit 1; fi

cd intygstyper
git checkout develop
cd $INTYG_HOME
if [ $? != 0 ]; then exit; fi

cd intygstjanst
git checkout develop
cd $INTYG_HOME
if [ $? != 0 ]; then exit; fi

cd minaintyg
git checkout develop
cd $INTYG_HOME
if [ $? != 0 ]; then exit; fi
	
cd schemas
git checkout develop
cd $INTYG_HOME
if [ $? != 0 ]; then exit; fi

cd tools
git checkout develop
cd $INTYG_HOME
if [ $? != 0 ]; then exit; fi
		
cd webcert
git checkout develop
cd $INTYG_HOME
if [ $? != 0 ]; then exit; fi
