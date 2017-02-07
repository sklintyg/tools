#!/bin/bash

scriptdir=$( cd $(dirname "${BASH_SOURCE[0]}") && pwd )

read -e -p "Use code quality tools? [y/n]? [y]: " QUALTOOLS

if [[ $QUALTOOLS =~ [nN] ]]; then
    $scriptdir/build_common.sh -c
else
    $scriptdir/build_common.sh -c -q
fi
