#!/usr/bin/env sh

set -e

if [ "x$DOCKER_REMOTE_PROTO" = "x" -o "x$DOCKER_REMOTE_HOST" = "x" -o "x$DOCKER_REMOTE_PORT" = "x" ]; then
	exit 0
fi

if [ "x$DEPENDENCIES" = "x" ]; then
	exit 0
fi

# waits until a container is "healthy"
# $1 dependent container name/id
wait_dependency() {
	tries=0
	endpoint="$DOCKER_REMOTE_PROTO://$DOCKER_REMOTE_HOST:$DOCKER_REMOTE_PORT/containers/$1/json"
	echo -n "waiting for $1 "
	while true; do
		if [ $tries -eq 60 ]; then
			echo "failed to start in 1 min - status: $status"
			exit 1
		fi

		status="$(curl -s "$endpoint" | jq '.State.Health.Status' | sed 's/"//g')"
		if [ "x$status" = "xhealthy" ]; then
			echo "ok"
			break
		fi

		tries=$((tries+1))
		sleep 1

		echo -n "."
	done
}

for dep in $DEPENDENCIES; do
	wait_dependency $dep
done
