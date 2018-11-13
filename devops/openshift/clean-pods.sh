#!/bin/bash

# Cleanup old pods

# Old is 1 day (24H)
OLD=1
OS=$(uname)

if [ "$OS" = "Darwin" ]; then
    BEFORE=$(date -v-${OLD}d +%s)
else
    BEFORE=$(date --date "${OLD} days ago" +%s)
fi

function clean() {
    name=$1
    if [ "$OS" = "Darwin" ]; then
        time=$(date -j -f%Y-%m-%dT%H:%M:%SZ "$2" +%s)
    else
        time=$(date --date "$2" +%s)
    fi
    if [ $time -lt $BEFORE ]; then
        oc delete pod "$name"
    fi
}

for pod in $(oc get pods -o go-template='{{range .items}}{{if and (ne .status.phase "New") (ne .status.phase "Running") (ne .status.phase "Pending")}}{{.metadata.name}}{{","}}{{.status.startTime}}{{"\n"}}{{end}}{{end}}')
do
    clean ${pod/,/ }
done
