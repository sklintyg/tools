#!/bin/bash
rm -rf filesexport
mkdir filesexport

MYPATH=`pwd`
$MYPATH/oc project sintyg
$MYPATH/oc get configmap intygstjanst-config -o yaml --export=true > filesexport/sintyg-intygstjanst.txt
$MYPATH/oc get configmap logsender-config -o yaml --export=true > filesexport/sintyg-logsender.txt
$MYPATH/oc get configmap minaintyg-config -o yaml --export=true > filesexport/sintyg-minaintyg.txt
$MYPATH/oc get configmap privatlakarportal-config -o yaml --export=true > filesexport/sintyg-privatlakarportal.txt
$MYPATH/oc get configmap rehabstod-config -o yaml --export=true > filesexport/sintyg-rehabstod.txt
$MYPATH/oc get configmap statistik-config -o yaml --export=true > filesexport/sintyg-statistik.txt
$MYPATH/oc get configmap webcert-config -o yaml --export=true > filesexport/sintyg-webcert.txt

$MYPATH/oc project pintyg
$MYPATH/oc get configmap intygstjanst-config -o yaml --export=true > filesexport/pintyg-intygstjanst.txt
$MYPATH/oc get configmap logsender-config -o yaml --export=true > filesexport/pintyg-logsender.txt
$MYPATH/oc get configmap minaintyg-config -o yaml --export=true > filesexport/pintyg-minaintyg.txt
$MYPATH/oc get configmap privatlakarportal-config -o yaml --export=true > filesexport/pintyg-privatlakarportal.txt
$MYPATH/oc get configmap rehabstod-config -o yaml --export=true > filesexport/pintyg-rehabstod.txt
$MYPATH/oc get configmap statistik-config -o yaml --export=true > filesexport/pintyg-statistik.txt
$MYPATH/oc get configmap webcert-config -o yaml --export=true > filesexport/pintyg-webcert.txt

rm -rf filesexportresult
mkdir filesexportresult
diff filesexport/sintyg-intygstjanst.txt filesexport/pintyg-intygstjanst.txt > filesexportresult/intygstjanst-diff.txt
diff filesexport/sintyg-logsender.txt filesexport/pintyg-logsender.txt > filesexportresult/logsender-diff.txt
diff filesexport/sintyg-minaintyg.txt filesexport/pintyg-minaintyg.txt > filesexportresult/minaintyg-diff.txt
diff filesexport/sintyg-privatlakarportal.txt filesexport/pintyg-privatlakarportal.txt > filesexportresult/privatlakarportal-diff.txt
diff filesexport/sintyg-rehabstod.txt filesexport/pintyg-rehabstod.txt > filesexportresult/rehabstod-diff.txt
diff filesexport/sintyg-statistik.txt filesexport/pintyg-statistik.txt > filesexportresult/statistik-diff.txt
diff filesexport/sintyg-webcert.txt filesexport/pintyg-webcert.txt > filesexportresult/webcert-diff.txt