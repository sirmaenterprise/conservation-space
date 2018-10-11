#!/usr/bin/env bash

set -e

if [ -n "$GDB_WORKERS" ]; then
	for worker in $GDB_WORKERS; do
		wait-for-service.sh -h $worker -p 8080
	done
fi