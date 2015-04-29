#!/bin/sh

function get_branch() {
      git branch --no-color | grep -E '^\*' | awk '{print $2}' || echo "default_value"
      # or
      #git symbolic-ref --short -q HEAD || echo "default_value";
}

export INTYG_HOME=`pwd`/../..
branch_name=`get_branch`

cd $INTYG_HOME/common-pom
echo "common-pom                 $(b=$(git symbolic-ref -q HEAD); { [ -n "$b" ] && echo ${b##refs/heads/}; } || echo HEAD)"
if [ $? != 0 ]; then exit 1; fi

cd $INTYG_HOME/schemas
echo "schemas                    $(b=$(git symbolic-ref -q HEAD); { [ -n "$b" ] && echo ${b##refs/heads/}; } || echo HEAD)"
if [ $? != 0 ]; then exit; fi

cd $INTYG_HOME/common
echo "common                     $(b=$(git symbolic-ref -q HEAD); { [ -n "$b" ] && echo ${b##refs/heads/}; } || echo HEAD)"
if [ $? != 0 ]; then exit 1; fi

cd $INTYG_HOME/tools
echo "tools                      $(b=$(git symbolic-ref -q HEAD); { [ -n "$b" ] && echo ${b##refs/heads/}; } || echo HEAD)"
if [ $? != 0 ]; then exit; fi

cd $INTYG_HOME/intygstyper
echo "intygstyper                $(b=$(git symbolic-ref -q HEAD); { [ -n "$b" ] && echo ${b##refs/heads/}; } || echo HEAD)"
if [ $? != 0 ]; then exit; fi

cd $INTYG_HOME/intygstjanst
echo "intygstjanst               $(b=$(git symbolic-ref -q HEAD); { [ -n "$b" ] && echo ${b##refs/heads/}; } || echo HEAD)"
if [ $? != 0 ]; then exit; fi

cd $INTYG_HOME/minaintyg
echo "minaintyg                  $(b=$(git symbolic-ref -q HEAD); { [ -n "$b" ] && echo ${b##refs/heads/}; } || echo HEAD)"
if [ $? != 0 ]; then exit; fi

cd $INTYG_HOME/webcert
echo "webcert                    $(b=$(git symbolic-ref -q HEAD); { [ -n "$b" ] && echo ${b##refs/heads/}; } || echo HEAD)"
if [ $? != 0 ]; then exit; fi
