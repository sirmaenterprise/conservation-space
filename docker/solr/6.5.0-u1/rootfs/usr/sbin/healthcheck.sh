#!/usr/bin/env bash

set -e

http_code=$(curl --fail -s -o /dev/null -I -w "%{http_code}" "http://0.0.0.0:8983/solr/admin/info/system?wt=json&_=$(date +%s)")
exit_code=$?

if [ "x$exit_code" != "x0" ]; then
	echo "Solr 6 is not healthy, HTTP code $http_code";
	exit 1;
fi

echo "Solr 6 is up and running!";
