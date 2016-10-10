#!/usr/bin/env bash
rm -r release
mkdir release
cd src/github.com/sklintyg/dbtool
gradle build
cp build/out/dbtool-linux-amd64 ../../../../release/dbtool
cp -r static ../../../../release
cd ../../../../release
jar cvf dbtool-1.0.zip .
cd ..
cp release/dbtool-1.0.zip ../ansible/roles/dbtool/templates