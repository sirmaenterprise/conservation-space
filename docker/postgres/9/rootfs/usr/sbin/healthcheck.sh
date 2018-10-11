#!/bin/bash

set -eu

if [ ! -s ${VOLUME_PGDATA}/pg_initialized ]; then
	echo "Service is not yet provisioned.";
	exit 1;
fi

# Checks if the service is up and accepts connections
gosu postgres pg_isready -h 127.0.0.1 -q
if [ ! $? = 0 ]; then
	echo "PostgreSQL service is not accepting connections!";
	exit 1;
fi

echo "PostgreSQL is up and running!";
