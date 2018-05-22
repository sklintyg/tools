#!/bin/bash
#
# Script to build and deploy and web applications.
#
# usage: webapp.sh [ -bcdhr ] [ -n <app_name> ] [ -m <build_version> ] [ -t <git_ref> ] [ -s <stage> ]
# -h: print usage options
#
# Config:
# Configurations are stored as a configmap and as a secret, which are mapped into the deployed application (see deploytemplate-webapp).
# Within the secret a resurze.zip file might exist, and this is unpacked upon application launch to the /tmp/resources folder.
#
# Build:
# The script assumes that the actual version (commit) of the code shall be built, i.e. it must have been previously pushed to remote.
# Typically a particular version tag/ref is built.
#
# Deploy:
# See deploytemplate-webapp
# 

function usage() {
    echo "usage: $(basename $0) [ -bcdhr ] [ -n <app_name> ] [ -m <build_version> ] [ -t <git_ref> ] [ -s <stage> ]"
    echo "  -b: do build"
    echo "  -c: do config"
    echo "  -d: do deploy"
    echo "  -h: prints usage options"
    echo "  -n <app_name>: set application name (default is git project name)"
    echo "  -m <build_version>: set build version (default is git tag)"
    echo "  -r: remove config, build or deploy  (in combination with other flags)"
    echo "  -s <stage>: stage name (default is test)"
    echo "  -t <git_ref>: build from git ref (default is current)"
    exit 1
}

while getopts "m:n:s:t:cbdh?r" opt; do
    case "$opt" in
	h|\?)
	    usage
	    ;;
	c) 
	    CONFIG=1
	    ;;
	m) 
	    BUILD_VERSION=$OPTARG
	    ;;
	n) 
	    APP_NAME=$OPTARG
	    ;;
	s) 
	    STAGE=$OPTARG
	    ;;
	d)
	    DEPLOY=1
	    ;;
	r)
	    REMOVE=1
	    ;;
	t) 
	    GIT_REF=$OPTARG
	    ;;
	b)
	    BUILD=1
	    ;;
    esac
done

PROJECT_DIR=$(git rev-parse --show-toplevel)
APP_NAME=${APP_NAME:-$(basename $PROJECT_DIR)}
STAGE=${STAGE:-test}
GIT_URL=$(git config --get remote.origin.url)
BUILD_VERSION=${BUILD_VERSION:-$(git describe --tags --dirty=.dirty)}
GIT_REF=${GIT_REF:-$(git rev-parse HEAD)}
RESOURCES=$(pwd)/$STAGE/env/resources.zip

function build() {
    oc process buildtemplate-webapp -p APP_NAME="$APP_NAME" -p GIT_URL="$GIT_URL" -p GIT_REF=$GIT_REF -p BUILD_VERSION=$BUILD_VERSION  -p STAGE=$STAGE | oc $1 -f -    
    [ $? != 0 ] && exit 1
    return 0
}

function deploy() {
    IMAGE=$(oc get is | grep "^${APP_NAME}\ " | awk '{ print $2 }')
    oc process deploytemplate-webapp -p APP_NAME="$APP_NAME" -p IMAGE="${IMAGE}:${BUILD_VERSION}" -p STAGE=$STAGE | oc $1 -f -
    [ $? != 0 ] && exit 1
    return 0
}

function assemble_resources() {
    echo "add: $1"
    [ -d "$1" ] && (cd $(dirname "$1"); zip -ru "$2" resources)
    return 0
}

function config() {
    assemble_resources $PROJECT_DIR/src/main/resources $RESOURCES
    oc create configmap "$APP_NAME-config" --from-file=$STAGE/config/
    oc create secret generic "$APP_NAME-env" --from-file=$STAGE/env/ --type=Opaque
    return 0
}

function exists() {
    oc get $1 | grep "^${2}\ "
    return $?
}


if [ ! -z "$REMOVE" ]; then
    [ ! -z "$BUILD" ] &&  exists bc "${APP_NAME}" && build delete
    [ ! -z "$DEPLOY" ] && exists dc  "${APP_NAME}" && deploy delete
    if [ ! -z "$CONFIG" ]; then
	rm -f $RESOURCES
	exists cm "${APP_NAME}-config" && oc delete cm "${APP_NAME}-config"
	exists secret "${APP_NAME}-env" && oc delete secret "${APP_NAME}-env"
    fi
    exit 0
fi

if [ ! -z "$CONFIG" ]; then
    config
fi

if [ ! -z "$BUILD" ]; then
    build apply && \
	oc start-build "${APP_NAME}-artifact" --follow --wait && \
	oc start-build "$APP_NAME" --follow --wait

fi

if [ ! -z "$DEPLOY" ]; then
    deploy apply
fi

