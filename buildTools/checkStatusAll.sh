#!/bin/bash

# Get project names
. __projects.sh

INTYG_HOME="${INTYG_HOME:-$( cd $(dirname "${BASH_SOURCE[0]}")/../.. && pwd )}"

for project in $ALL_PROJECTS 
do
    echo $project
    cd "$INTYG_HOME/$project"
    git status -sb
    echo
done
