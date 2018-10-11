#!/bin/sh
set -eu

for file in $@; do
	if [ -e $file ]; then
  		awk -f /usr/sbin/replace-env-vars.awk "${file}" > "${file}.tmp";
  		mv "${file}.tmp" "${file}"
  	else
  		echo "The specified file ${file} does NOT exist!";
  	fi
done
