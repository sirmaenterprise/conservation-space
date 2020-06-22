#!/usr/bin/env sh

status="$(curl -s -o /dev/null -w "%{http_code}" http://0.0.0.0:8080/emf/api/ping)"
if [ "x$status" != "x200" ]; then
	exit 1
fi
