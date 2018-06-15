#!/bin/bash

function clean() {
	name=$1
	tags=($(echo $(oc get is $name --template='{{range .status.tags}}{{.tag}}{{"\n"}}{{end}}')))
	n_tags=${#tags[@]}
	if [ $n_tags -gt 10 ]; then
		n_rem=$((${n_tags} - 10))
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
