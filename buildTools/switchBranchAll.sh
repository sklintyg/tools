#!/bin/bash

# Get project names
. __projects.sh

INTYG_HOME="${INTYG_HOME:-$( cd $(dirname "${BASH_SOURCE[0]}")/../.. && pwd )}"

branch=${1:-develop}

for project in $ALL_PROJECTS 
do
    echo $project
    cd "$INTYG_HOME/$project"
    exists=$(git rev-parse --verify --quiet $branch)
    if [ -z "$exists" ]; then
        git checkout -b $branch
    else
        git checkout $branch
    fi
    echo
done
