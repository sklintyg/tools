#!/bin/sh

export INTYG_HOME=`pwd`/../..

read -p "Please enter the name of you feature branch : " featureBranch
read -p "$featureBranch will be closed in all projects is this ok? y/n : " RESP

if [ "$RESP" = "y" ]; then 
	
	cd $INTYG_HOME/common
	git flow feature finish $featureBranch
	if [ $? != 0 ]; then exit 1; fi

	#cd $INTYG_HOME/tools
	#git flow feature finish $featureBranch
	#if [ $? != 0 ]; then exit; fi

	cd $INTYG_HOME/schemas
	git flow feature finish $featureBranch
	if [ $? != 0 ]; then exit; fi

	cd $INTYG_HOME/intygstyper
	git flow feature finish $featureBranch
	if [ $? != 0 ]; then exit; fi

	cd $INTYG_HOME/intygstjanst
	git flow feature finish $featureBranch
	if [ $? != 0 ]; then exit; fi

	cd $INTYG_HOME/minaintyg
	git flow feature finish $featureBranch
	if [ $? != 0 ]; then exit; fi

	cd $INTYG_HOME/webcert
	git flow feature finish $featureBranch
	if [ $? != 0 ]; then exit; fi
	
else
	echo "bye!"	
fi
