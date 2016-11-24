#!/bin/bash

INTYG_HOME="${INTYG_HOME:-$( cd $(dirname "${BASH_SOURCE[0]}")/../.. && pwd )}"

branch=${1:-develop}

for project in schemas common intygstyper intygstjanst minaintyg webcert; do
    echo $project
    cd "$INTYG_HOME/$project"
    git checkout $branch
    echo
done
