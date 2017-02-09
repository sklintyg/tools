#!/bin/bash

_display_help() {
    echo "Usage: $0 [OPTION...]"
    echo "Builds the core 'Intygsprojektet'."
    echo "Default behaviour is to run unit tests but no 'clean' and no code quality tools."
    echo
    echo "   -h, --help              shows this message"
    echo "   -c, --clean             clean before building"
    echo "   -q, --code-quality      apply code rules check; failures are treated as errors"
    echo "   -xt, --exclude-tests    do not run unit tests"
    echo
}

projects=(common infra intygstjanst minaintyg webcert)
command="./gradlew --parallel build install "
flags=""
clean=false

for arg; do
    case $arg in
        -h|--help) _display_help && exit 0 ;;
        -c|--clean) clean=true ;;
        -q|--code-quality) flags+="-PcodeQuality " ;;
        -xt|--exclude-tests) flags+="-xtest " ;; # No tests
        *) _display_help && exit 1 ;;
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
