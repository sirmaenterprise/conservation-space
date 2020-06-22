#!/usr/bin/env sh

set +u

echo "Setting wildfly env..."

sync

./bin/add-user.sh -u $DOCKER_USER -p $DOCKER_USER

MANAGEMENT="-Dserver.mgmt.user=$DOCKER_USER -Dserver.mgmt.pass=$DOCKER_USER -Djboss.bind.address.management=0.0.0.0"
export JAVA_OPTS="$JAVA_OPTS -Dconfig.path=$SERVICE_DIR_WILDFLY_HOME/standalone/configuration/sep/config.properties $MANAGEMENT"
