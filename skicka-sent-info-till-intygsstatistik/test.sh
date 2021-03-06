#!/usr/bin/env sh

DB_URL=
DB_DRIVER=
DB_USERNAME=
DB_PASSWORD=
BROKER_URL=
BROKER_QUEUE=
BROKER_USERNAME=
BROKER_PASSWORD=
DATE_FROM=
DATE_TO=
DEBUG=true

# Exempel
#DB_URL="jdbc:mysql://localhost:3306/intyg?useCompression=true"
#DB_DRIVER="com.mysql.jdbc.Driver"
#DB_USERNAME="abcde"
#DB_PASSWORD="12345"
#BROKER_URL="tcp://localhost:61616"
#BROKER_QUEUE="intyg.statistik.test"
#BROKER_USERNAME="abcde"
#BROKER_PASSWORD="12345"
#DATE_FROM="2016-12-01"
#DATE_TO="2017-08-31"
#DEBUG=true

if [ -n "$DATE_FROM" ]; then
  if [ -z "$DATE_TO" ]; then
    DATE_TO=$(date +'%Y-%m-%d')
  fi
fi
if [ ! -z $DB_URL ]; then
  ARGS+="-PDB_URL=$DB_URL "
fi
if [ ! -z $DB_DRIVER ]; then
  ARGS+="-PDB_DRIVER=$DB_DRIVER "
fi
if [ ! -z $DB_USERNAME ]; then
  ARGS+="-PDB_USERNAME=$DB_USERNAME "
fi
if [ ! -z $DB_PASSWORD ]; then
  ARGS+="-PDB_PASSWORD=$DB_PASSWORD "
fi
if [ ! -z $BROKER_URL ]; then
  ARGS+="-PBROKER_URL=$BROKER_URL "
fi
if [ ! -z $BROKER_QUEUE ]; then
  ARGS+="-PBROKER_QUEUE=$BROKER_QUEUE "
fi
if [ ! -z $BROKER_USERNAME ]; then
  ARGS+="-PBROKER_USERNAME=$BROKER_USERNAME "
fi
if [ ! -z $BROKER_PASSWORD ]; then
  ARGS+="-PBROKER_PASSWORD=$BROKER_PASSWORD "
fi
if [ ! -z $DATE_FROM ]; then
  ARGS+="-PDATE_FROM=$DATE_FROM "
fi
if [ ! -z $DATE_TO ]; then
  ARGS+="-PDATE_TO=$DATE_TO "
fi
if [ ! -z $DEBUG ]; then
  ARGS+="-PDEBUG=$DEBUG"
fi

./gradlew $ARGS
