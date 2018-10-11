#!/bin/sh

set -e

curl -k -s http://127.0.0.1:8080/ > /dev/null
if [ ! $? = 0 ]; then
	echo "GraphDB is not ready yet!";
	exit 1;
fi

echo "GraphDB is up and running!";
