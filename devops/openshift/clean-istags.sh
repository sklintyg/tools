#!/bin/bash

# Cleanup image stream tags, only keep a maximum number

KEEP=10
function clean() {
	name=$1
	tags=($(echo $(oc get is $name --template='{{range .status.tags}}{{.tag}}{{"\n"}}{{end}}')))
	n_tags=${#tags[@]}
	if [ ${n_tags} -gt ${KEEP} ]; then
		n_rem=$((${n_tags} - ${KEEP}))
		for tag in ${tags[*]}
		do
			oc delete istag/${name}:${tag}
			n_rem=$((${n_rem} - 1))
			[ ${n_rem} -eq 0 ] && break
		done
	fi
}

for img in $(oc get is -o go-template='{{range .items}}{{.metadata.name}}{{"\n"}}{{end}}')
do
	clean $img
done
