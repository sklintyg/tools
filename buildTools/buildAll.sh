#!/bin/bash

read -e -p "Use code quality tools? [y/n]? [y]: " QUALTOOLS

if [[ $QUALTOOLS =~ [nN] ]]; then
    ./build_common.sh -c
else
    ./build_common.sh -c -q
fi
