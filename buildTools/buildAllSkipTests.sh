#!/bin/bash

INTYG_HOME="$( cd $(dirname "${BASH_SOURCE[0]}")/../.. && pwd )"

for project in schemas common intygstyper intygstjanst minaintyg webcert; do
    cd "$INTYG_HOME/$project"
    gradle clean assemble install || exit 1
done
