#!/bin/bash

CMD="./gradlew --parallel build install -PcodeQuality"

read -e -p "Use checkstyle [y/n]? [y]: " PROCEED

[[ $PROCEED =~ [nN] ]] && CMD="./gradlew --parallel build install"

INTYG_HOME="${INTYG_HOME:-$( cd $(dirname "${BASH_SOURCE[0]}")/../.. && pwd )}"

start_time=$(date +%s)

# Building with --parallel does not guarentee that 'clean' is run in the correct order. We therefore iterate twice.
for project in common intygstyper intygstjanst minaintyg webcert; do
    cd "$INTYG_HOME/$project"
    ./gradlew clean
done

for project in common intygstyper intygstjanst minaintyg webcert; do
    cd "$INTYG_HOME/$project"
    ${CMD} "$@" || exit 1
done

duration=$(( $(date +%s) - $start_time ))
echo
echo Build using script $(basename "$0") completed at $(date +%T) after a total of $(($duration / 60)) min $(($duration % 60)) sec.
