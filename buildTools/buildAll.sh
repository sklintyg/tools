#!/bin/sh

CMD="mvn clean install checkstyle:check"
DEFAULT="y"
read -e -p "Use checkstyle:check [y/n]? [y]: " PROCEED
# adopt the default, if 'enter' given
PROCEED="${PROCEED:-${DEFAULT}}"
# change to lower case to simplify following if
PROCEED="${PROCEED,,}"

# condition for specific letter
if [ "${PROCEED}" == "n" ] ; then
  CMD="mvn clean install" 
fi

export INTYG_HOME=`pwd`/../..


cd $INTYG_HOME/common-pom
mvn clean install -Ddependency.unpack-skip=true $@
if [ $? != 0 ]; then exit 1; fi

cd $INTYG_HOME/schemas
mvn clean install $@
if [ $? != 0 ]; then exit; fi

cd $INTYG_HOME/common
${CMD} $@
if [ $? != 0 ]; then exit 1; fi

cd $INTYG_HOME/tools
${CMD} $@
if [ $? != 0 ]; then exit; fi

cd $INTYG_HOME/intygstyper
${CMD} $@
if [ $? != 0 ]; then exit; fi

cd $INTYG_HOME/intygstjanst
${CMD} $@
if [ $? != 0 ]; then exit; fi

cd $INTYG_HOME/minaintyg
${CMD} $@
if [ $? != 0 ]; then exit; fi

cd $INTYG_HOME/webcert
${CMD} $@
if [ $? != 0 ]; then exit; fi
