#!/bin/bash

CMD="./gradlew clean build install -PcodeQuality"

read -e -p "Use checkstyle:check [y/n]? [y]: " PROCEED

[[ $PROCEED =~ [nN] ]] && CMD="./gradlew clean build install"

INTYG_HOME="$( cd $(dirname "${BASH_SOURCE[0]}")/../.. && pwd )"

for project in common intygstyper intygstjanst minaintyg webcert; do
    cd "$INTYG_HOME/$project"
    ${CMD} "$@" || exit 1
done
