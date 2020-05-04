#!/bin/bash
####################
# Detta script körs på maskinen som har mysqldatabasen för Intygsjansten.
#
# Usage: ./gdpr_pp_part2_it.sh <database_user> <database_password> <it_database_name>
#
# Två textfiler med databasdumpar från privatläkarportalen läses in, pp_gdpr_query_result.txt och pp_allavardgivare_dump.txt
# Dessa måste ligga i working directory vid körning. Efter att scriptet körs så läggs resultatet i wc_siths_units_result.txt i working directory.
####################

# Set to swedish locale, for correct encoding of characters
LC_ALL="sv_SE.utf8"
LC_CTYPE="sv_SE.utf8"

USER=$1
PASSWORD=$2

IT_DATABASE_NAME=$3

WEBCERT_QUERY_RESULT="$(cat wc_siths_units_result.txt)"
SITHS_UNITS="$(cat wc_siths_units.txt)"

QUERY2="SELECT\
    a.CARE_UNIT_ID,\
    a.latest_included,\
    a.oldest_included,\
    a.active_months,\
    a.total_nr,\
    CASE\
        WHEN active_months = 0 THEN a.total_nr\
        ELSE a.total_nr / active_months\
    END intyg_per_month\
 FROM\
    (SELECT\
        c.CARE_UNIT_ID,\
		MAX(c.SIGNED_DATE) AS latest_included,\
		MIN(c.SIGNED_DATE) AS oldest_included,\
		TIMESTAMPDIFF(MONTH, MIN(c.SIGNED_DATE), MAX(c.SIGNED_DATE)) AS active_months,\
		COUNT(c.ID) AS total_nr\
    FROM\
        CERTIFICATE c\
    WHERE\
       TIMESTAMPDIFF(MONTH, c.SIGNED_DATE, NOW()) < 3\
       AND\
        ($(echo "$SITHS_UNITS" | sed -r 's/(.+)/c.CARE_UNIT_ID=\x27\1\x27 or /' | tr "\n" " " | sed -r 's/\s+or\s+$//g') )\
    GROUP BY c.CARE_UNIT_ID) a \
 ORDER BY a.CARE_UNIT_ID;"

echo "Executing query: $QUERY2"
IT_SITHS_UNITS_QUERY_RESULT="$(mysql --user=$USER --password=$PASSWORD --batch -e "use $IT_DATABASE_NAME; $QUERY2")"

#Time for mixing the results
CLEANED_MAIN_DATA="$(echo "$WEBCERT_QUERY_RESULT" | grep -F "${SITHS_UNITS}" | sort -k 1)"
CLEANED_AUGMENT_DATA="$(echo "${IT_SITHS_UNITS_QUERY_RESULT}" | grep -F "${SITHS_UNITS}" | sort -k 1)"

echo "$CLEANED_MAIN_DATA" > partial_result_1.txt
echo "$CLEANED_AUGMENT_DATA" > partial_result_2.txt

echo -e "id\temail\ttelefonnummer\tpostadress\tpostnummer\tpostort\tlatest_included\toldest_included\ttotal_nr\tintyg_per_month" > wc_siths_units_result.txt
#outer left join, on id
echo "$(join -t $'\t' -a 1 -1 1 -2 1 partial_result_1.txt partial_result_2.txt)" >> wc_siths_units_result.txt

echo "Finished script! Results found in wc_siths_units_result.txt"
