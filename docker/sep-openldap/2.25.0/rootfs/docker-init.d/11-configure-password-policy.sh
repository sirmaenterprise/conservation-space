#!/usr/bin/env bash

set -eu

function provision() {
	wait-for-service.sh -h 127.0.0.1 -p 10389 &>/dev/null
	checkProvisioned || configurePasswordPolicy
}

function checkProvisioned() {
	ENTRY_COUNT=`ldapsearch -Y EXTERNAL -Q -H ldapi:// -LLL -b "cn=config" -s sub "(ObjectClass=olcPPolicyConfig)" dn | grep dn: | wc -l`
	if [ $ENTRY_COUNT -eq "0" ]; then
		return 1
	fi
}

function configurePasswordPolicy() {
	echo "================ Staring password policy configuration ================"

	replace-env.sh /docker-init.d/11-ppolicy-overlay.ldif
	replace-env.sh /docker-init.d/11-hash-method.ldif

	# load bundled password policy schema
	ldapmodify -a -D $SEP_MDB_ROOT_DN -H ldapi:// -Y EXTERNAL -f /etc/openldap/schema/ppolicy.ldif

	# install the password policy module
	ldapmodify -a -D $SEP_MDB_ROOT_DN -H ldapi:// -Y EXTERNAL -f /docker-init.d/11-ppolicy-module.ldif

	# configure password policy for mdb used by SEP
	ldapmodify -a -D $SEP_MDB_ROOT_DN -H ldapi:// -Y EXTERNAL -f /docker-init.d/11-ppolicy-overlay.ldif

	# set password hash method on base db - frontend
    ldapmodify -a -D $SEP_MDB_ROOT_DN -H ldapi:// -Y EXTERNAL -f /docker-init.d/11-hash-method.ldif

	echo "================ Configured password policy ================"
}

# Schedule the provisioning after LDAP is up & running
provision &
