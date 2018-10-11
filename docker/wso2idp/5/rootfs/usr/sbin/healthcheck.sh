#!/bin/sh

set -e

curl -k -s https://127.0.0.1:9443/carbon/ > /dev/null;
if [ ! $? = 0 ]; then
	echo "WSO2 Identity provider is not up yet";
	exit 1;
fi

echo "WSO2 Identity provider is up and running!";
