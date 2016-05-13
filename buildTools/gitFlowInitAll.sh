#!/bin/sh

export INTYG_HOME=`pwd`/../..

cd $INTYG_HOME/common
git checkout --track origin/master
git flow init
if [ $? != 0 ]; then exit 1; fi

cd $INTYG_HOME/intygstyper
git checkout --track origin/master
git flow init
if [ $? != 0 ]; then exit; fi

cd $INTYG_HOME/intygstjanst
git checkout --track origin/master
git flow init
if [ $? != 0 ]; then exit; fi

cd $INTYG_HOME/minaintyg
git checkout --track origin/master
git flow init
if [ $? != 0 ]; then exit; fi
	
cd $INTYG_HOME/schemas
git checkout --track origin/master
git flow init
if [ $? != 0 ]; then exit; fi

cd $INTYG_HOME/tools
git checkout --track origin/master
git flow init
if [ $? != 0 ]; then exit; fi
		
cd $INTYG_HOME/webcert
git checkout --track origin/master
git flow init
if [ $? != 0 ]; then exit; fi
