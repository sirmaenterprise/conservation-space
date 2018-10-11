#!/usr/bin/env bash

set -eu

if [[ ! $(ls -A ${LDAP_CONFIG_DIR}) ]]; then
	slapadd -d 64 -d 128 -n0 -F ${LDAP_CONFIG_DIR} -l /etc/openldap/slapd.ldif
	chown -R ${DOCKER_USER}:${DOCKER_USER} ${LDAP_CONFIG_DIR}
	echo "" && echo "================ Provisioned base OpenLDAP schema ================" && echo ""
fi
