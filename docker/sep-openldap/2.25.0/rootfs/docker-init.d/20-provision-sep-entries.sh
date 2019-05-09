#!/usr/bin/env bash

set -eu

LDIF_PATH=/docker-init.d/20-base-sep-entries.ldif

function provision() {
	wait-for-service.sh -h 127.0.0.1 -p 10389 &>/dev/null
	checkProvisioned || importBaseData
}

function checkProvisioned() {
	ldapsearch -Y EXTERNAL -Q -H ldapi:// -LLL -b "$SEP_MDB_SUFFIX" -s sub "(ObjectClass=organization)"
}

function importBaseData() {
	replace-env.sh $LDIF_PATH
	ldapmodify -a -x -D $SEP_MDB_ROOT_DN -w $SEP_MDB_DEFAULT_PASS -H ldapi:// -f $LDIF_PATH
	echo "================ Imported base SEP entries ================"
}

# Schedule the provisioning after LDAP is up & running
provision &