#!/bin/bash
####################
# Detta script körs på maskinen som har mysqldatabasen för Intygsjansten.
#
# Usage: ./webcert_usage_part1_it.sh <database_user> <database_password> <it_database_name>
#
# Två textfiler med databasdumpar från privatläkarportalen läses in, wc_usage_query_result.txt och pp_allavardgivare_dump.txt
# Dessa måste ligga i working directory vid körning. Efter att scriptet körs så läggs resultatet i wc_pp_usage_query_result.txt i working directory.
####################

# Set to swedish locale, for correct encoding of characters
LC_ALL="sv_SE.utf8"
LC_CTYPE="sv_SE.utf8"

USER=$1
PASSWORD=$2
# 'intygstjanst' is used in development.
INTYGSTJANST_DATABASE_NAME=$3

PRIVATLAKARPORTAL_QUERY_RESULT="$(cat wc_pp_usage_query_result.txt)"
VARDGIVARE_I_PP="$(cat pp_allavardgivare_dump.txt)"

# hsa_id_vårdgivare, antal_intyg, senaste_intyg_inkommet
QUERY="SELECT\
    CERTIFICATE.CARE_GIVER_ID AS hsa_id_vardgivare,\
    COUNT(CERTIFICATE.CARE_GIVER_ID) AS antal_intyg,\
    COUNT(CERTIFICATE.CARE_GIVER_ID)/3.0 AS antal_intyg_per_manad,\
    MIN(ORIGINAL_CERTIFICATE.RECEIVED) AS forsta_intyg_inkommet,\
    MAX(ORIGINAL_CERTIFICATE.RECEIVED) AS senaste_intyg_inkommet\
 FROM\
    CERTIFICATE\
        INNER JOIN\
    ORIGINAL_CERTIFICATE ON CERTIFICATE.ID = ORIGINAL_CERTIFICATE.CERTIFICATE_ID\
 WHERE\
    TIMESTAMPDIFF(MONTH, ORIGINAL_CERTIFICATE.RECEIVED, NOW()) < 3\
	AND\
		($(echo "$VARDGIVARE_I_PP" | sed -r 's/(.+)/CARE_GIVER_ID=\x27\1\x27 or /' | tr "\n" " " | sed -r 's/\s+or\s+$//g') )\
 group by CERTIFICATE.CARE_GIVER_ID order by CERTIFICATE.CARE_GIVER_ID;"

echo "Executing query: $QUERY"
INTYGSTJANST_QUERY_RESULT="$(mysql --user=$USER --password=$PASSWORD --batch -e "use $INTYGSTJANST_DATABASE_NAME; $QUERY")"

#Time for mixing the results
CLEANED_MAIN_DATA="$(echo "$PRIVATLAKARPORTAL_QUERY_RESULT" | grep -F "${VARDGIVARE_I_PP}" | sort -k 1)"
CLEANED_AUGMENT_DATA="$(echo "${INTYGSTJANST_QUERY_RESULT}" | grep -F "${VARDGIVARE_I_PP}" | sort -k 1)"

echo "$CLEANED_MAIN_DATA" > partial_result_1.txt
echo "$CLEANED_AUGMENT_DATA" > partial_result_2.txt

echo -e "VARDGIVARE_ID\tPERSONID\tVARDGIVARE_NAMN\tEPOST\tTELEFONNUMMER\tantal_intyg\tantal_intyg_per_manad\tforsta_intyg_i_urval\tsenaste_intyg_i_urval" > pp_gdpr_dump_result.txt
#outer left join, on VARDGIVARE_ID
echo "$(join -t $'\t' -a 1 -1 1 -2 1 partial_result_1.txt partial_result_2.txt)" >> wc_pp_usage_query_result.txt
