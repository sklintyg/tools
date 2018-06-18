#!/bin/bash

USER=$1
PASSWORD=$2

QUERY="select VARDGIVARE_ID,PERSONID,VARDGIVARE_NAMN,EPOST from PRIVATLAKARE order by VARDGIVARE_ID;"
echo "Executing query: $QUERY"
PRIVATLAKARPORTAL_QUERY_RESULT="$(mysql --user=$USER --password=$PASSWORD --batch -e "use privatlakarportal; $QUERY")"
echo "$PRIVATLAKARPORTAL_QUERY_RESULT" > pp_gdpr_query_result.txt

QUERY="select VARDGIVARE_ID from PRIVATLAKARE order by VARDGIVARE_ID;"
echo "Executing query: $QUERY"
VARDGIVARE_I_PP="$(mysql --user=$USER --password=$PASSWORD --batch -e "use privatlakarportal; $QUERY" | tail -n +2)"
# Skapa en lista av samtliga privatläkare
echo "$VARDGIVARE_I_PP" > pp_allavardgivare_dump.txt


