#!/usr/bin/env bash

set -eu
set +vx

SECRET=/run/secrets/sep-mdb-password

if [[ $(ls -A ${LDAP_CONFIG_DIR}) ]]; then
	return 0
fi

# Reading & hashing the root password
mdb_pass=${SEP_MDB_DEFAULT_PASS}
if [ -f ${SECRET} ]; then
	echo "Using mounted secret to use for ${SEP_MDB_ROOT_DN}'s root password"
	mdb_pass=$(cat ${SECRET})
fi
hashed_pass=$(slappasswd -h "{SSHA}" -s ${mdb_pass})

# Replacing variables
sed "s|\$sep_mdb_pass_replace|${hashed_pass}|" -i /etc/openldap/slapd.ldif
replace-env.sh /etc/openldap/slapd.ldif

# Loading schema and deleting the file with replaced variables.
slapadd -d 64 -d 128 -n0 -F ${LDAP_CONFIG_DIR} -l /etc/openldap/slapd.ldif
rm -f /etc/openldap/slapd.ldif

# The script is executed as root so the permissions must be restored
chown -R ${DOCKER_USER}:${DOCKER_USER} ${LDAP_CONFIG_DIR}
echo -e "\n================ Provisioned SEP's OpenLDAP schema ================\n"
