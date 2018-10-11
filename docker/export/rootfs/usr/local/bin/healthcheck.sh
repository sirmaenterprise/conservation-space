#!/usr/bin/env bash

set -eu

status=`curl -sf -o /dev/null -w "%{http_code}" http://0.0.0.0:8080/export/health`
if [ "x$status" != "x200" ]; then
	exit 1
fi
