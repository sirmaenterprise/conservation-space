#!/usr/bin/env bash

set -eu

script_dir="$( cd "$( dirname "$0" )" && pwd )"

for file in $@; do
	if [ -s $file ]; then
  		awk -f "$script_dir/replace-env-vars.awk" "${file}" > "${file}.tmp";
  		sync
  		mv "${file}.tmp" "${file}"
  	else
  		echo "The specified file ${file} does NOT exist!";
  	fi
done
