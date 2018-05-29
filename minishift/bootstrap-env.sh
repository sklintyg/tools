#!/bin/bash
eval $(minishift oc-env)
oc login -u developer -p developer
oc new-project labbtest
oc project labbtest
# oc adm policy add-scc-to-user anyuid -z default
oc policy add-role-to-group     system:image-puller system:serviceaccounts:labbtest     --namespace=intygstjanster-test

