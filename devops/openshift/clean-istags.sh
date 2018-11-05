#!/bin/bash

# Cleanup image stream tags, only keep a maximum number

if [ "$#" -ne 2 ]; then
	echo "usage: clean-istags.sh <is_name> <major_version>"
	exit 1
fi

KEEP=10
IS_NAME=$1
MAJOR_VERSION=$2

if [ ${#MAJOR_VERSION} -lt 5 ]; then
	echo "Error: major MAJOR_version must be at least charaacters, i.e X.Y.Z"
	exit 1
fi


function clean() {
	tags=($(echo $(oc get is $IS_NAME --template='{{range .status.tags}}{{.tag}}{{"\n"}}{{end}}' | grep ^${MAJOR_VERSION} | sort -t '.'  -n -b +3)))
	n_tags=${#tags[@]}

	if [ ${n_tags} -gt ${KEEP} ]; then
		n_rem=$((${n_tags} - ${KEEP}))
		for tag in ${tags[*]}
		do
			oc delete istag/${IS_NAME}:${tag}
			n_rem=$((${n_rem} - 1))
			[ ${n_rem} -eq 0 ] && break
		done
	fi
}

clean

