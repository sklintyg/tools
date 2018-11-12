#!/bin/bash

# Cleanup old pods

# Old is 1 day (24H)
OLD=1
OS=$(uname)
case "$OS" in
    "Linux")
        BEFORE=$(date --date "${OLD} days ago" +%s)
        ;;
    "Darwin")
        BEFORE=$(date -v-${OLD}d +%s)
        ;;
    *)
        echo "$OS: Unsupported platform (date function)"
        exit 1
        ;;
esac

function clean() {
    name=$1
    case "$OS" in
        "Linux")
            time=$(date --date "$2" +%s)
            ;;
        "Darwin")
            time=$(date -j -f%Y-%m-%dT%H:%M:%SZ "$2" +%s)
            ;;
    esac
    if [ $time -lt $BEFORE ]; then
        oc delete pod "$name"
    fi
}

for pod in $(oc get pods -o go-template='{{range .items}}{{if and (ne .status.phase "New") (ne .status.phase "Running") (ne .status.phase "Pending")}}{{.metadata.name}}{{","}}{{.status.startTime}}{{"\n"}}{{end}}{{end}}')
do
    clean ${pod/,/ }
done
