#!/bin/bash

test -d /node_modules && echo "Update node_modules cache" && cp -a $1/node_modules/. /node_modules/ || true
test -d /jspm_packages && echo "Update jspm_packages cache" && cp -a $1/jspm_packages/. /jspm_packages/ || true