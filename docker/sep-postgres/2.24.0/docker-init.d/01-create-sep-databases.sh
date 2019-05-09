#!/usr/bin/env sh

createDbIfAbsent() {
	psql --username postgres -tc "SELECT 1 FROM pg_database WHERE datname = '$1'" | grep -q 1 || psql --username postgres -c "CREATE DATABASE $1 WITH OWNER $2"
}

gosu $DOCKER_USER pg_ctl -D "$PGDATA" -o "-c listen_addresses='localhost'" -w start

if [ ! -s "$PGDATA/sep_provisioned" ]; then

	psql --username postgres <<-EOSQL
		-- Roles
		CREATE ROLE admin WITH LOGIN SUPERUSER PASSWORD '$ROLE_PASS_ADMIN' CREATEDB CREATEROLE;
		CREATE ROLE sep WITH LOGIN PASSWORD '$ROLE_PASS_SEP';
	EOSQL

	date >> "$PGDATA/sep_provisioned"
else

	psql --username postgres <<-EOSQL
		ALTER USER admin WITH SUPERUSER PASSWORD '$ROLE_PASS_ADMIN';
		ALTER USER sep WITH SUPERUSER PASSWORD '$ROLE_PASS_SEP';
	EOSQL

fi

createDbIfAbsent admin admin
createDbIfAbsent sep sep
createDbIfAbsent sepaudit sep
createDbIfAbsent alfresco sep
createDbIfAbsent wso2is sep
createDbIfAbsent keycloak sep

gosu $DOCKER_USER pg_ctl -D "$PGDATA" -m fast -w stop
