#!/bin/bash

_display_help() {
    echo "Usage: $0 [BRANCH]"
    echo "Switches to branch [BRANCH] in all projects."
    echo "The 'develop' branch is assumed if omitted."
    echo
    echo "   -h, --help              shows this message"
    echo
}

for arg; do
    case $arg in
        -h|--help) _display_help && exit 0 ;;
    esac
done

scriptdir=$(cd $( dirname "${BASH_SOURCE[0]}") &&  pwd)
# Get project names
. ${scriptdir}/__projects.sh

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
