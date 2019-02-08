#!/bin/bash
rm -rf exports
mkdir exports

MYPATH=`pwd`
$MYPATH/oc project sintyg
$MYPATH/oc get configmap intygstjanst-configmap-envvar -o yaml --export=true > exports/sintyg-intygstjanst.txt
$MYPATH/oc get configmap logsender-configmap-envvar -o yaml --export=true > exports/sintyg-logsender.txt
$MYPATH/oc get configmap minaintyg-configmap-envvar -o yaml --export=true > exports/sintyg-minaintyg.txt
$MYPATH/oc get configmap privatlakarportal-configmap-envvar -o yaml --export=true > exports/sintyg-privatlakarportal.txt
$MYPATH/oc get configmap rehabstod-configmap-envvar -o yaml --export=true > exports/sintyg-rehabstod.txt
$MYPATH/oc get configmap statistik-configmap-envvar -o yaml --export=true > exports/sintyg-statistik.txt
$MYPATH/oc get configmap webcert-configmap-envvar -o yaml --export=true > exports/sintyg-webcert.txt

$MYPATH/oc project pintyg
$MYPATH/oc get configmap intygstjanst-configmap-envvar -o yaml --export=true > exports/pintyg-intygstjanst.txt
$MYPATH/oc get configmap logsender-configmap-envvar -o yaml --export=true > exports/pintyg-logsender.txt
$MYPATH/oc get configmap minaintyg-configmap-envvar -o yaml --export=true > exports/pintyg-minaintyg.txt
$MYPATH/oc get configmap privatlakarportal-configmap-envvar -o yaml --export=true > exports/pintyg-privatlakarportal.txt
$MYPATH/oc get configmap rehabstod-configmap-envvar -o yaml --export=true > exports/pintyg-rehabstod.txt
$MYPATH/oc get configmap statistik-configmap-envvar -o yaml --export=true > exports/pintyg-statistik.txt
$MYPATH/oc get configmap webcert-configmap-envvar -o yaml --export=true > exports/pintyg-webcert.txt

rm -rf result
mkdir result
diff exports/sintyg-intygstjanst.txt exports/pintyg-intygstjanst.txt > result/intygstjanst-diff.txt
diff exports/sintyg-logsender.txt exports/pintyg-logsender.txt > result/logsender-diff.txt
diff exports/sintyg-minaintyg.txt exports/pintyg-minaintyg.txt > result/minaintyg-diff.txt
diff exports/sintyg-privatlakarportal.txt exports/pintyg-privatlakarportal.txt > result/privatlakarportal-diff.txt
diff exports/sintyg-rehabstod.txt exports/pintyg-rehabstod.txt > result/rehabstod-diff.txt
diff exports/sintyg-statistik.txt exports/pintyg-statistik.txt > result/statistik-diff.txt
diff exports/sintyg-webcert.txt exports/pintyg-webcert.txt > result/webcert-diff.txt