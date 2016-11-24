#!/bin/bash

INTYG_HOME="${INTYG_HOME:-$( cd $(dirname "${BASH_SOURCE[0]}")/../.. && pwd )}"

for project in schemas common intygstyper intygstjanst minaintyg webcert; do
    cd "$INTYG_HOME/$project"
    branch=$(git rev-parse --abbrev-ref HEAD)
    [[ $branch == develop ]] && branchcolor=32 || branchcolor=31
    printf '%-16s\e['$branchcolor'm%s\e[00m\n' $project $branch
done
