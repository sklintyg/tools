#!/bin/bash

projects=(common infra intygstjanst minaintyg webcert)
command="./gradlew --parallel build install "
flags=""
clean=false

for arg; do
    case $arg in
        -c) clean=true ;;
        -q) flags+="-PcodeQuality " ;;
        -xt) flags+="-xtest " ;; # No tests
    esac
done

# If INTYG_HOME is set, use it as root dir. Otherwise calculate root dir from script location.
INTYG_HOME="${INTYG_HOME:-$( cd $(dirname "${BASH_SOURCE[0]}")/../.. && pwd )}"

start_time=$(date +%s)

# Building with --parallel does not guarantee that 'clean' is run in the correct order. We therefore run 'clean' in its own loop.
if [[ $clean == true ]]; then
    for project in ${projects[@]}; do
        cd "$INTYG_HOME/$project"
        ./gradlew clean
    done
fi

for project in ${projects[@]}; do
    cd "$INTYG_HOME/$project"
    $command $flags || exit 1
done

duration=$(( $(date +%s) - $start_time ))

echo $'\n'Build completed at $(date +%T) after a total of $(($duration / 60)) min, $(($duration % 60)) sec.
