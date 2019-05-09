#!/usr/bin/env bash

set -e

wget -O /dev/null -q http://127.0.0.1:8080/auth
if [ ! $? = 0 ]; then
	echo "Keycloak is not ready yet!";
	exit 1;
fi

echo "Keycloak is up and running!";
