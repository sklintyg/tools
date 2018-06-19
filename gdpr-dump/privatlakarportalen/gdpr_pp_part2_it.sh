#!/bin/bash
####################
# Usage: ./gdpr_pp_part2_it.sh <database_user> <database_password> <it_database_name>
# Två textfiler med databasdumpar från privatläkarportalen läses in, pp_gdpr_query_result.txt och pp_allavardgivare_dump.txt
# Dessa måste ligga i working directory vid körning. Efter att scriptet körs så läggs resultatet i pp_gdpr_dump_result.txt i working directory.
####################

USER=$1
PASSWORD=$2
# 'intygstjanst' is used in development.
INTYGSTJANST_DATABASE_NAME=$3

PRIVATLAKARPORTAL_QUERY_RESULT="$(cat pp_gdpr_query_result.txt)"
VARDGIVARE_I_PP="$(cat pp_allavardgivare_dump.txt)"

# hsa_id_vårdgivare, antal_intyg, senaste_intyg_inkommet
QUERY="select CERTIFICATE.CARE_GIVER_ID as hsa_id_vardgivare,COUNT(CERTIFICATE.CARE_GIVER_ID) as antal_intyg,MAX(ORIGINAL_CERTIFICATE.RECEIVED) as senaste_intyg_inkommet \
from CERTIFICATE inner join ORIGINAL_CERTIFICATE \
				on CERTIFICATE.ID=ORIGINAL_CERTIFICATE.CERTIFICATE_ID \
where $(echo "$VARDGIVARE_I_PP" | sed -r 's/(.+)/CARE_GIVER_ID=\x27\1\x27 or /' | tr "\n" " " | sed -r 's/\s+or\s+$//g') \
group by CERTIFICATE.CARE_GIVER_ID order by CERTIFICATE.CARE_GIVER_ID;"

echo "Executing query: $QUERY"
INTYGSTJANST_QUERY_RESULT="$(mysql --user=$USER --password=$PASSWORD --batch -e "use $INTYGSTJANST_DATABASE_NAME; $QUERY")"

#Time for mixing the results
CLEANED_MAIN_DATA="$(echo "$PRIVATLAKARPORTAL_QUERY_RESULT" | grep -F "${VARDGIVARE_I_PP}" | sort -k 1)"
CLEANED_AUGMENT_DATA="$(echo "${INTYGSTJANST_QUERY_RESULT}" | grep -F "${VARDGIVARE_I_PP}" | sort -k 1)"

echo -e "VARDGIVARE_ID\tPERSONID\tVARDGIVARE_NAMN\tEPOST\tantal_intyg\tsenaste_intyg_inkommet_datum" > pp_gdpr_dump_result.txt
#outer left join, on VARDGIVARE_ID
echo "$(join -t $'\t' -a 1 -1 1 -2 1 <(echo "$CLEANED_MAIN_DATA") <(echo "$CLEANED_AUGMENT_DATA"))" >> pp_gdpr_dump_result.txt


