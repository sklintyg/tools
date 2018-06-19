#!/bin/bash
####################
# Detta script körs på maskinen som har mysqldatabasen för Intygsjansten.
#
# Usage: ./gdpr_pp_part2_it.sh <database_user> <database_password> <it_database_name>
#
# En textfil med databasdumpar skapas i current working directory, it_gdpr_partial_result.txt
# Dessa används av nästa script, gdpr_it_part2_pp.sh
####################

USER=$1
PASSWORD=$2
# 'intygstjanst' is used in development.
INTYGSTJANST_DATABASE_NAME=$3

#Vårdgivare HSA-id,Antal registrerade intyg (vårdgivare),Datum för senast registrerade intyg (vårdgivare),Är vårdgivaren registrerad i Privatläkarportalen
QUERY="select CERTIFICATE.CARE_GIVER_ID as hsa_id_vardgivare,COUNT(CERTIFICATE.CARE_GIVER_ID) as antal_intyg,MAX(ORIGINAL_CERTIFICATE.RECEIVED) as senaste_intyg_inkommet_datum \
from CERTIFICATE inner join ORIGINAL_CERTIFICATE \
       on CERTIFICATE.ID=ORIGINAL_CERTIFICATE.CERTIFICATE_ID \
group by CERTIFICATE.CARE_GIVER_ID order by CERTIFICATE.CARE_GIVER_ID;"

echo "Executing query: $QUERY"
INTYGSTJANST_QUERY_RESULT="$(mysql --user=$USER --password=$PASSWORD --batch -e "use $INTYGSTJANST_DATABASE_NAME; $QUERY")"
echo "$INTYGSTJANST_QUERY_RESULT" > it_gdpr_partial_result.txt

