#!/bin/bash
####################
# Usage: ./gdpr_pp_part1_pp.sh <database_user> <database_password> <pp_database_name>
# Två textfiler med databasdumpar skapas i current working directory, pp_gdpr_query_result.txt och pp_allavardgivare_dump.txt
# Dessa används av nästa script, gdpr_pp_part2_it.sh
####################

USER=$1
PASSWORD=$2
# In development, we use 'privatlakarportal'
PRIVATLAKARPORTAL_DATABASE_NAME=$3

QUERY="select VARDGIVARE_ID,PERSONID,VARDGIVARE_NAMN,EPOST from PRIVATLAKARE order by VARDGIVARE_ID;"
echo "Executing query: $QUERY"
PRIVATLAKARPORTAL_QUERY_RESULT="$(mysql --user=$USER --password=$PASSWORD --batch -e "use $PRIVATLAKARPORTAL_DATABASE_NAME; $QUERY")"
# For use in final result
echo "$PRIVATLAKARPORTAL_QUERY_RESULT" > pp_gdpr_query_result.txt

# Skapa en lista av samtliga privatläkare
VARDGIVARE_I_PP="$(echo "$PRIVATLAKARPORTAL_QUERY_RESULT" | cut -f 1 | tail -n +2)"
echo "$VARDGIVARE_I_PP" > pp_allavardgivare_dump.txt


