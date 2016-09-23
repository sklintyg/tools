#!/bin/bash

CMD="./gradlew clean build install -PcodeQuality"

read -e -p "Use checkstyle:check [y/n]? [y]: " PROCEED

[[ $PROCEED =~ [nN] ]] && CMD="./gradlew clean build install"

INTYG_HOME="$( cd $(dirname "${BASH_SOURCE[0]}")/../.. && pwd )"

start_time=`date +%s`

for project in common intygstyper intygstjanst minaintyg webcert; do
    cd "$INTYG_HOME/$project"
    ${CMD} "$@" || exit 1
done

duration=$(expr `date +%s` - $start_time)
echo Build completed at `date +%T` after a total of  $(($duration / 60)) min $(($duration % 60)) sec
