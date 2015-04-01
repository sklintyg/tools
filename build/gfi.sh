#!/bin/sh

export INTYG_HOME=`pwd`/../..

cd common
git checkout --track origin/master
git flow init
cd $INTYG_HOME
if [ $? != 0 ]; then exit 1; fi

cd intygstyper
git checkout --track origin/master
git flow init
cd $INTYG_HOME
if [ $? != 0 ]; then exit; fi

cd intygstjanst
git checkout --track origin/master
git flow init
cd $INTYG_HOME
if [ $? != 0 ]; then exit; fi

cd minaintyg
git checkout --track origin/master
git flow init
cd $INTYG_HOME
if [ $? != 0 ]; then exit; fi
	
cd schemas
git checkout --track origin/master
git flow init
cd $INTYG_HOME
if [ $? != 0 ]; then exit; fi

cd tools
git checkout --track origin/master
git flow init
cd $INTYG_HOME
if [ $? != 0 ]; then exit; fi
		
cd webcert
git checkout --track origin/master
git flow init
cd $INTYG_HOME
if [ $? != 0 ]; then exit; fi
