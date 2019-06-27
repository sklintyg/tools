#!/bin/bash

# Get project names
. __projects.sh

INTYG_HOME="${INTYG_HOME:-$( cd $(dirname "${BASH_SOURCE[0]}")/../.. && pwd )}"

branch=${1}

if [ -z "$branch" ]; then
    echo "You must provide a branch..."
    exit 1
fi

if [ "$branch" == "develop" ] || [ "$branch" == "master" ]; then
    echo "You're not allowed to delete the branches 'develop' and 'master'."
    exit 1
fi

read -e -p "Delete '$branch' in all projects? [y/n]? [n]: " DBRANCH

if [[ "$DBRANCH" =~ [yY] ]]; then
    for project in $ALL_PROJECTS 
    do
        echo $project
        cd "$INTYG_HOME/$project"
        exists=$(git rev-parse --verify --quiet $branch)
        if [ -z "$exists" ]; then
            echo "branch '$branch' not found."
        else
            git checkout --quiet develop; git branch -D $branch;
        fi
        echo
    done
else
    echo "Nothing was deleted!"
fi

