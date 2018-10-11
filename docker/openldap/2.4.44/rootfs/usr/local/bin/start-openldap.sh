#!/usr/bin/env bash
set -eu

# Reduce maximum number of number of open file descriptors to 1024 otherwise slapd consumes two orders of magnitude more of RAM
# see https://github.com/docker/docker/issues/8231
ulimit -n 1024

# By default OpenLDAP will try to bind on 389 but non root users cannot bind on ports under 1024
slapd -h "ldap://0.0.0.0:10389 ldapi:///" -d ${LDAP_DEBUG_LEVEL} -u ${DOCKER_USER} -g ${DOCKER_USER} -F ${LDAP_CONFIG_DIR}
