#!/bin/bash

# Accepted arguments:
# first argument - path to the main webapp directory (the nodejs project)

set -e

cd $1

echo "Compile seip-ui:$2"
gulp compile

echo "Run unit tests on ${UNIT_TEST_BROWSER}"
gulp test --development=false --browsers=${UNIT_TEST_BROWSER}

echo "Kill hanged chrome instances if any"
pkill -9 chrome || true