#!/usr/bin/env sh

gosu $DOCKER_USER pg_ctl -D "$PGDATA" -o "-c listen_addresses='localhost'" -w start

if [ ! -s "$PGDATA/sep_provisioned" ]; then

	psql --username postgres <<-EOSQL
		-- Roles
		CREATE ROLE admin WITH LOGIN SUPERUSER PASSWORD '$ROLE_PASS_ADMIN' CREATEDB CREATEROLE;
		CREATE ROLE sep WITH LOGIN PASSWORD '$ROLE_PASS_SEP';

		-- Databases
		CREATE DATABASE admin WITH OWNER admin;
		CREATE DATABASE sep WITH OWNER sep;
		CREATE DATABASE sepaudit WITH OWNER sep;
		CREATE DATABASE alfresco WITH OWNER sep;
		CREATE DATABASE wso2is WITH OWNER sep;
	EOSQL


	date >> "$PGDATA/sep_provisioned"
else

	psql --username postgres <<-EOSQL
		ALTER USER admin WITH SUPERUSER PASSWORD '$ROLE_PASS_ADMIN';
		ALTER USER sep WITH SUPERUSER PASSWORD '$ROLE_PASS_SEP';
	EOSQL
fi

gosu $DOCKER_USER pg_ctl -D "$PGDATA" -m fast -w stop
