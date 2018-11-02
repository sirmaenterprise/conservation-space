#!/usr/bin/env bash

set -e

if [ -n "$REMOTE_DEBUG" ]; then
	export EXTRA_CLI_ARGS="$EXTRA_CLI_ARGS --debug 8787"
fi
