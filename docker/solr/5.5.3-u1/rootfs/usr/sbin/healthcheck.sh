#!/usr/bin/env bash

set -e

curl --fail -s "http://localhost:8983/solr/admin/info/system?wt=json&_=$(date +%s)"
if [ ! $? = 0 ]; then
	echo "Solr 5 is not up yet";
	exit 1;
fi

echo "Solr 5 is up and running!";
