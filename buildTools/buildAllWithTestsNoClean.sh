#!/bin/bash

INTYG_HOME="$( cd $(dirname "${BASH_SOURCE[0]}")/../.. && pwd )"

start_time=$(date +%s)

for project in common intygstyper intygstjanst minaintyg webcert; do
    cd "$INTYG_HOME/$project"
    ./gradlew --parallel build install || exit 1
done

duration=$(( $(date +%s) - $start_time ))
echo
echo Build using script $(basename "$0") completed at $(date +%T) after a total of $(($duration / 60)) min $(($duration % 60)) sec.