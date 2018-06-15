#!/bin/bash

# Cleanup old pods

# Old is 1 day (24h)
OLD=1d
BEFORE=$(date -v-${OLD} +%s)

function clean() {
    name=$1
    time=$(date -j -f%Y-%m-%dT%H:%M:%SZ "$2" +%s)
    if [ $time -lt $BEFORE ]; then
        oc delete pod "$name"
    fi
}

for pod in $(oc get pods -o go-template='{{range .items}}{{if and (ne .status.phase "Running") (ne .status.phase "Pending")}}{{.metadata.name}}{{","}}{{.status.startTime}}{{"\n"}}{{end}}{{end}}')
do
    clean ${pod/,/ }
done
