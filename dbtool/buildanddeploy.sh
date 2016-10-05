#!/usr/bin/env bash

export GOARCH=amd64
export GOOS=linux
go build -o bin/dbtool src/github.com/sklintyg/dbtool/*.go
export GOARCH=amd64
export GOOS=darwin
cp bin/dbtool release/dbtool
cp -r static release
cd release
jar cvf dbtool-1.0.zip .
cd ..
cp release/dbtool-1.0.zip ../ansible/roles/dbtool/templates