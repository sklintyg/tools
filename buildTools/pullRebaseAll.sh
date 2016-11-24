#!/bin/bash

INTYG_HOME="${INTYG_HOME:-$( cd $(dirname "${BASH_SOURCE[0]}")/../.. && pwd )}"

for project in schemas common intygstyper intygstjanst minaintyg webcert; do
    echo $project
    cd "$INTYG_HOME/$project"
    git pull --rebase origin || exit 1
    echo
done
