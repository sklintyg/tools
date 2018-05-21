#!/bin/bash

while getopts "n:s:cbdh?r" opt; do
    case "$opt" in
	h|\?)
            echo "usage: $(basename $0) [ -n <app_name> [ -s <stage> ] [ -bcdr ]"
	    echo "-b: do build (default)"
	    echo "-b: do config (default)"
	    echo "-d: do deploy"
	    echo "-r: remove artifact"
            exit 1
            ;;
	c) 
	    CONFIG=1
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
	b)
	    BUILD=1
	    ;;
    esac
done

[ -z "$APP_NAME" ] && APP_NAME=$(cd ..; basename $(pwd))
[ -z "$STAGE" ] && STAGE=test

GIT_URL=$(git config --get remote.origin.url)
BUILD_VERSION=$(git describe --tags --dirty=.dirty)
GIT_REF=$(git rev-parse HEAD)
RESOURCES=$(pwd)/env/$STAGE/resources.zip
PROJECT_DIR=$(git rev-parse --show-toplevel)

function build() {
    oc process buildtemplate-webapp -p APP_NAME="$APP_NAME" -p GIT_URL="$GIT_URL" -p GIT_REF=$GIT_REF -p BUILD_VERSION=$BUILD_VERSION  -p STAGE=$STAGE | oc $1 -f -    
    [ $? != 0 ] && exit 1
    return 0
}

function deploy() {
    IMAGE=$(oc get is | grep "^${APP_NAME}\ " | grep $BUILD_VERSION | awk '{ print $2 }')
    oc process deploytemplate-webapp -p APP_NAME="$APP_NAME" -p IMAGE="$IMAGE" -p STAGE=$STAGE | oc $1 -f -
    [ $? != 0 ] && exit 1
    return 0
}

function assemble_resources() {
    echo "assemble: $1, $2"
    [ -d "$1" ] && (cd $(dirname "$1"); zip -ru "$2" resources)
    return 0
}

function config() {
    assemble_resources $PROJECT_DIR/main/resources $RESOURCES
    assemble_resources $PROJECT_DIR/web/src/main/resources $RESOURCES
    oc create configmap "$APP_NAME-config" --from-file=config/$STAGE/
    oc create secret generic "$APP_NAME-env" --from-file=env/$STAGE/ --type=Opaque
}

if [ ! -z "$REMOVE" ]; then
    [ ! -z "$BUILD" ] &&  oc get bc | grep "^${APP_NAME}\ " && build delete
    [ ! -z "$DEPLOY" ] && oc get dc | grep "^${APP_NAME}\ " && deploy delete
    if [ ! -z "$CONFIG" ]; then
	rm -f $RESOURCES
	oc delete configmap "$APP_NAME-config"
	oc delete secret "$APP_NAME-env"
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

