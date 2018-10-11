#!/bin/sh

# posgres looks for this var
export PGDATA="$VOLUME_PGDATA"

mkdir -p /run/postgresql
chmod g+s /run/postgresql
chown -R $DOCKER_USER /run/postgresql

# replace env vars in conf files then include them in main config
for f in /etc/postgresql/*;
do
	replace-env.sh $f
done

if [ ! -s "$PGDATA/PG_VERSION" ]; then
	echo "!!!Please ignore message: FATAL:  role 'dockeru' does not exist, see this http://dba.stackexchange.com/a/44480"
	gosu $DOCKER_USER initdb --username=postgres

	gosu $DOCKER_USER pg_ctl -D "$PGDATA" -o "-c listen_addresses='localhost'" -w start

	psql --username postgres <<-EOSQL
		ALTER USER postgres WITH SUPERUSER PASSWORD '$POSTGRES_PASS' ;
	EOSQL

	{ echo; echo "host all all 0.0.0.0/0 md5"; } >> "$PGDATA/pg_hba.conf"
	echo "include_dir '/etc/postgresql'" >> "$PGDATA/postgresql.conf"

	gosu $DOCKER_USER pg_ctl -D "$PGDATA" -m fast -w stop
fi

# using named volumes with compose file seems to always reset the permissions to 755
# pg requires that only the owner has access to the data dir - overriding here
chmod 700 $PGDATA
sync

date >> $PGDATA/pg_initialized
