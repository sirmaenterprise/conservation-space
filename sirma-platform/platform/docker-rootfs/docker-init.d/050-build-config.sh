#!/usr/bin/env sh

set -eu

echo "Building wildfly config.properties..."

CONFIG_PROPS="$SERVICE_DIR_WILDFLY_HOME/standalone/configuration/sep/config.properties"

if [ -d $SERVICE_DIR_WILDFLY_HOME/standalone/configuration/sep/properties ]; then
	for f in $SERVICE_DIR_WILDFLY_HOME/standalone/configuration/sep/properties/*.properties; do
		if [ -f "$f" ]; then
			cat $f >> $CONFIG_PROPS
			echo >> $CONFIG_PROPS
		fi
	done
fi

replace-env.sh $CONFIG_PROPS
replace-env.sh $SERVICE_DIR_WILDFLY_HOME/standalone/configuration/standalone.xml
chown $DOCKER_USER:$DOCKER_USER $CONFIG_PROPS $SERVICE_DIR_WILDFLY_HOME/standalone/configuration/standalone.xml

sync
