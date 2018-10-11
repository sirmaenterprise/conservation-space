#!/usr/bin/env sh

set -e

if [ -d /opt/wso2is/repository/resources/keystore ]; then
	cp -af /opt/wso2is/repository/resources/keystore/. /opt/wso2is/repository/resources/security/
fi

if [ ! -f /opt/wso2is/repository/data/is-default-schema.zip ]; then
	cp /opt/wso2is/repository/data-dir-init/is-default-schema.zip /opt/wso2is/repository/data/is-default-schema.zip
fi

if [ -z "$HOST_NAME" ]; then
	export HOST_NAME="$HOSTNAME"
fi

if [ -z "$LDAP_HOST" ]; then
	export LDAP_HOST="$HOST_NAME"
fi

for file in /opt/wso2is/repository/conf/*.xml; do
    replace-env.sh "$file"
done

replace-env.sh /opt/wso2is/repository/conf/datasources/master-datasources.xml
replace-env.sh /opt/wso2is/repository/conf/axis2/axis2.xml
replace-env.sh /opt/wso2is/repository/conf/security/identity-mgt.properties

chown -R $DOCKER_USER:$DOCKER_USER /opt/wso2is
sync
