#!/usr/bin/env bash

set -eu

sync

# Adding initial user to ignore the welcome message
${SERVICE_DIR_KEYCLOAK}/bin/add-user-keycloak.sh -u ${KEYCLOAK_USER} -p ${KEYCLOAK_PASS}
