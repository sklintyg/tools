#!/bin/sh

export INTYG_HOME=`pwd`/../..

cd common
git flow init
cd $INTYG_HOME
if [ $? != 0 ]; then exit 1; fi

cd intygstyper
git flow init
cd $INTYG_HOME
if [ $? != 0 ]; then exit; fi

cd intygstjanst
git flow init
cd $INTYG_HOME
if [ $? != 0 ]; then exit; fi

cd minaintyg
git flow init
cd $INTYG_HOME
if [ $? != 0 ]; then exit; fi
	
cd schemas
git flow init
cd $INTYG_HOME
if [ $? != 0 ]; then exit; fi

cd tools
git flow init
cd $INTYG_HOME
if [ $? != 0 ]; then exit; fi
		
cd webcert
git flow init
cd $INTYG_HOME
if [ $? != 0 ]; then exit; fi
