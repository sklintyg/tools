#!/bin/bash
####################
# Detta script körs på maskinen som har mysqldatabasen för Privatläkarportalen.
#
# Usage: ./gdpr_pp_part1_pp.sh <database_user> <database_password> <pp_database_name>
#
# En textfil databasdump från intygstjänsten läses in, it_gdpr_partial_result.txt
# Dennaa måste ligga i working directory vid körning. Efter att scriptet körs så läggs resultatet i it_gdpr_dump_result.txt i working directory.
####################

USER=$1
PASSWORD=$2
# In development, we use 'privatlakarportal'
PRIVATLAKARPORTAL_DATABASE_NAME=$3

INTYGSTJANST_QUERY_RESULT="$(cat it_gdpr_partial_result.txt)"
VARDGIVARE_I_IT="$(echo "$INTYGSTJANST_QUERY_RESULT" | cut -f 1 | tail -n +2)"


QUERY="select VARDGIVARE_ID,'JA' as finns_i_pp from PRIVATLAKARE \
where $(echo "$VARDGIVARE_I_IT" | sed -r 's/(.+)/VARDGIVARE_ID=\x27\1\x27 or /' | tr "\n" " " | sed -r 's/\s+or\s+$//g') \
order by VARDGIVARE_ID;"
echo "Executing query: $QUERY"
PRIVATLAKARPORTAL_QUERY_RESULT="$(mysql --user=$USER --password=$PASSWORD --batch -e "use $PRIVATLAKARPORTAL_DATABASE_NAME; $QUERY")"

#Time for mixing the results
CLEANED_MAIN_DATA="$(echo "$INTYGSTJANST_QUERY_RESULT" | tail -n +2 | sort -k 1)"
CLEANED_AUGMENT_DATA="$(echo "${PRIVATLAKARPORTAL_QUERY_RESULT}" | tail -n +2 | sort -k 1)"
#Vårdgivare HSA-id,Antal registrerade intyg (vårdgivare),Datum för senast registrerade intyg (vårdgivare),Är vårdgivaren registrerad i Privatläkarportalen
echo -e "vardgivare_hsa_id\tantal_registrerade_intyg\tdatum_for_senast_registrerade_intyg\tvardgivare_registrerad_i_pp" > it_gdpr_dump_result.txt
#outer left join, on VARDGIVARE_ID
echo "$(join -t $'\t' -a 1 -1 1 -2 1 <(echo "$CLEANED_MAIN_DATA") <(echo "$CLEANED_AUGMENT_DATA"))" >> it_gdpr_dump_result.txt

