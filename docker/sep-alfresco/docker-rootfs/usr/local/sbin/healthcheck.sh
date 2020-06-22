#!/bin/sh

set -e

wget -O /dev/null -q http://127.0.0.1:8080/alfresco/service/api/server;
if [ ! $? = 0 ]; then
	echo "Alfresco is not up yet!";
	exit 1;
fi

echo "Alfresco is up and running!";
