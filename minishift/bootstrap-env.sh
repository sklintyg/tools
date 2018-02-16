#!/bin/bash
eval $(minishift oc-env)
oc login -u developer -p developer
oc new-project labbtest
oc project labbtest
# oc adm policy add-scc-to-user anyuid -z default
oc policy add-role-to-group     system:image-puller system:serviceaccounts:labbtest     --namespace=intygstjanster-test

oc create -R -f templates/mysql
oc create -R -f templates/activemq
#oc create -R -f templates/logsender/deploy
#oc create -R -f templates/intygstjanst/deploy
#oc create -R -f templates/webcert/deploy

