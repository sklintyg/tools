#!/usr/bin/env sh

DB_URL=
DB_DRIVER=
DB_USERNAME=
DB_PASSWORD=
BROKER_URL=
BROKER_QUEUE=
BROKER_USERNAME=
BROKER_PASSWORD=
INTYGSTYPER=
DATE_FROM=
DATE_TO=
DEBUG=false

# Exempel
#DB_URL="jdbc:mysql://localhost:3306/intyg?useCompression=true"
#DB_DRIVER="com.mysql.jdbc.Driver"
#DB_USERNAME="abcde"
#DB_PASSWORD="12345"
#BROKER_URL="tcp://localhost:61616"
#BROKER_QUEUE="intyg.statistik.test"
#BROKER_USERNAME="abcde"
#BROKER_PASSWORD="12345"
#INTYGSTYPER="'luse','lisjp'"
#DATE_FROM="2016-12-01"
#DATE_TO="2017-08-31"
#DEBUG=true

if [ -n "$DATE_FROM" ]; then
  if [ -z "$DATE_TO" ]; then
    DATE_TO=$(date +'%Y-%m-%d')
  fi
fi
if [ ! -z $DB_URL ]; then
  ARGS+="DB_URL=$DB_URL "
fi
if [ ! -z $DB_DRIVER ]; then
  ARGS+="DB_DRIVER=$DB_DRIVER "
fi
if [ ! -z $DB_USERNAME ]; then
  ARGS+="DB_USERNAME=$DB_USERNAME "
fi
if [ ! -z $DB_PASSWORD ]; then
  ARGS+="DB_PASSWORD=$DB_PASSWORD "
fi
if [ ! -z $BROKER_URL ]; then
  ARGS+="BROKER_URL=$BROKER_URL "
fi
if [ ! -z $BROKER_QUEUE ]; then
  ARGS+="BROKER_QUEUE=$BROKER_QUEUE "
fi
if [ ! -z $BROKER_USERNAME ]; then
  ARGS+="BROKER_USERNAME=$BROKER_USERNAME "
fi
if [ ! -z $BROKER_PASSWORD ]; then
  ARGS+="BROKER_PASSWORD=$BROKER_PASSWORD "
fi
if [ ! -z $INTYGSTYPER ]; then
  ARGS+="INTYGSTYPER=$INTYGSTYPER "
fi
if [ ! -z $DATE_FROM ]; then
  ARGS+="DATE_FROM=$DATE_FROM "
fi
if [ ! -z $DATE_TO ]; then
  ARGS+="DATE_TO=$DATE_TO "
fi
if [ ! -z $DEBUG ]; then
  ARGS+="DEBUG=$DEBUG"
fi

APP_HOME="`pwd -P`"
APP_NAME="skicka-intyg-till-intygsstatistik"

RUNCMD="$APP_HOME/bin/$APP_NAME"

exec $RUNCMD $ARGS
