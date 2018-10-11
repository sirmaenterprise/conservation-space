#!/usr/bin/env bash

status=`curl -s -o /dev/null -I -w "%{http_code}" http://0.0.0.0:8300/health`
if [ "x$status" != "x200" ]; then
    echo "The preview service's health check returned HTTP status code: $status"
    exit 1
fi
