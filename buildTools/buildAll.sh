#!/bin/bash

CMD="gradle clean build install -PcodeQuality"

read -e -p "Use checkstyle:check [y/n]? [y]: " PROCEED

[[ $PROCEED =~ [nN] ]] && CMD="gradle clean build install"

INTYG_HOME="$( cd $(dirname "${BASH_SOURCE[0]}")/../.. && pwd )"

for project in schemas common intygstyper intygstjanst minaintyg webcert; do
    cd "$INTYG_HOME/$project"
    ${CMD} "$@" || exit 1
done
