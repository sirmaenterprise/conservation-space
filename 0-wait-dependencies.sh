#!/usr/bin/env sh

set -e

if [ -n "$SERVICE_DEPENDENCIES" ]; then

	for dep in $SERVICE_DEPENDENCIES; do

		host="$(echo $dep | cut -d':' -f1)"
		port="$(echo $dep | cut -d':' -f2)"

		wait-for-service.sh -h $host -p $port
	done
fi
