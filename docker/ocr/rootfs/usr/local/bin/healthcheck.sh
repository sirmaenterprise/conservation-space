#!/bin/sh

status=`curl -s -I -o /dev/null -w "%{http_code}" http://0.0.0.0:8200/health`
if [ "x$status" != "x200" ]; then
    echo "The service's health check returned HTTP status code: $status"
	exit 1
fi