#!/bin/bash
####################
# Detta script körs på maskinen som har mysqldatabasen för webcert.
#
# Usage: ./webcert_siths_usage.sh <database_user> <database_password> <wc_database_name>
#
# Två textfiler med databasdumpar skapas i current working directory, wc_siths_units_result.txt och wc_siths_units.txt
# Dessa används av nästa script, webcert_siths_usage_it_part2.sh
####################

# Set to swedish locale, for correct encoding of characters
LC_ALL="sv_SE.utf8"
LC_CTYPE="sv_SE.utf8"

USER=$1
PASSWORD=$2

WEBCERT_DATABASE_NAME=$3


QUERY="SELECT DISTINCT\
    i.ENHETS_ID\
 FROM\
    INTYG i\
        JOIN\
    SIGNATUR s ON i.INTYGS_ID = s.INTYG_ID\
        LEFT JOIN\
    INTEGRERADE_VARDENHETER iv ON i.ENHETS_ID = iv.ENHETS_ID\
 WHERE\
    s.SIGNATUR_TYP = 'XMLDSIG'\
    AND\
    iv.ENHETS_ID IS NULL\
 ORDER BY i.ENHETS_ID"
echo "Executing query: $QUERY"
UNITS_QUERY_RESULT="$(mysql --user=$USER --password=$PASSWORD --batch -e "use $WEBCERT_DATABASE_NAME; $QUERY")"

SITHS_UNITS="$(echo "$UNITS_QUERY_RESULT" | cut -f 1 | tail -n +2)"
echo "$SITHS_UNITS" > wc_siths_units.txt

QUERY2="SELECT \
    t.ENHETS_ID as id,\
    JSON_UNQUOTE(JSON_EXTRACT(t.json, '\$.epost')) AS email,\
    JSON_UNQUOTE(JSON_EXTRACT(t.json, '\$.telefonnummer')) AS telefonnummer,\
    JSON_UNQUOTE(JSON_EXTRACT(t.json, '\$.postadress')) AS postadress,\
    JSON_UNQUOTE(JSON_EXTRACT(t.json, '\$.postnummer')) AS postnummer,\
    JSON_UNQUOTE(JSON_EXTRACT(t.json, '\$.postort')) AS postort\
 FROM\
    (SELECT \
		i.ENHETS_ID,\
		JSON_EXTRACT(CONVERT( ANY_VALUE(i.model) USING UTF8), '\$.grundData.skapadAv.vardenhet') AS json\
    FROM INTYG i\
    WHERE $(echo "$SITHS_UNITS" | sed -r 's/(.+)/i.ENHETS_ID=\x27\1\x27 or /' | tr "\n" " " | sed -r 's/\s+or\s+$//g') \
    GROUP BY i.ENHETS_ID) t\
 ORDER BY t.ENHETS_ID;"

echo "Executing query: $QUERY2"
WC_SITHS_UNITS_QUERY_RESULT="$(mysql --user=$USER --password=$PASSWORD --batch -e "use $WEBCERT_DATABASE_NAME; $QUERY2")"
echo "$WC_SITHS_UNITS_QUERY_RESULT" > wc_siths_units_result.txt

echo "Finished script!"
