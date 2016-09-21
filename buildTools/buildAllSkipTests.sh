#!/bin/bash

INTYG_HOME="$( cd $(dirname "${BASH_SOURCE[0]}")/../.. && pwd )"

for project in common intygstyper intygstjanst minaintyg webcert; do
    cd "$INTYG_HOME/$project"
    echo $(pwd)
    ./gradlew clean assemble install || exit 1
done
