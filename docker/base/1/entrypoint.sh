#!/bin/sh
set -e

changeown() {
	for dir in $1; do
		if [ -d "$dir" ]; then
			chown -R $DOCKER_USER:$DOCKER_USER $dir
		fi
	done
}

echo "################# env #################"
env
echo "#######################################"

echo "Setting permissions on data dirs (env | grep ^VOLUME_)"
VOLUME_DIRS="$(env | grep ^VOLUME_ | cut -d'=' -f2)"
changeown "$VOLUME_DIRS"

echo "Setting permissions on service dirs (env | grep ^SERVICE_DIR_)"
SERVICE_DIRS="$(env | grep ^SERVICE_DIR_ | cut -d'=' -f2)"
changeown "$SERVICE_DIRS"

echo "Running /docker-init.d scripts"
if [ -d /docker-init.d ]; then
	for f in /docker-init.d/*.sh; do
		[ -f "$f" ] && . "$f"
	done
fi

sync

set +u

wait-dependencies.sh

if [ -n "$RUN_AS_ROOT" ]; then
	echo "Running $@ as root"
	gosu root "$@"
else
	echo "Running $@"
	gosu $DOCKER_USER "$@"
fi
