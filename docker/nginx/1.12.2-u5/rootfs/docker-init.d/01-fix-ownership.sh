#!/usr/bin/env bash

set -eu

chown -R $DOCKER_USER:$DOCKER_USER /var/tmp/nginx /var/cache/nginx /var/log/nginx
