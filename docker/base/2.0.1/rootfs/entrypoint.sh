#!/usr/bin/env bash

set -e

changeown() {
	for dir in $1; do
		# if the root dir has the correct ownership - assume alles gut
		if [ "$(stat -c '%U:%G' $dir)" != "$DOCKER_USER:$DOCKER_USER" ]; then
			echo "Changing ownership of $dir"
			chown -R $DOCKER_USER:$DOCKER_USER $dir
		fi
	done
}

echo -e "\n################# env #################"
env
echo -e "#######################################\n"

usermod -u $DOCKER_USER_ID $DOCKER_USER
groupmod -g $DOCKER_USER_GROUP_ID $DOCKER_USER

# syncing here to workaround "resource busy" problems for mounted files
sync

echo "Setting ownership ($DOCKER_USER:$DOCKER_USER) on volume and service dirs"
changeown "$(env | grep -e ^SERVICE_DIR_ -e ^VOLUME_ | cut -d'=' -f2)"

echo "Running /docker-init.d scripts"
if [ -d /docker-init.d ]; then
	for f in /docker-init.d/*.sh; do
		[ -f "$f" ] && . "$f"
	done
fi

sync

set +u

if [ -n "$RUN_AS_ROOT" ]; then
	echo "Running $@ as root"
	gosu root "$@"
else
	echo "Running $@"
	gosu $DOCKER_USER "$@"
fi
