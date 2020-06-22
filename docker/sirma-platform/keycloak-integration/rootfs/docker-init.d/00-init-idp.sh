#!/usr/bin/env sh

set -e

replace-env.sh ${SERVICE_DIR_KEYCLOAK}/bin/sep-configure-datasource.cli
replace-env.sh ${SERVICE_DIR_KEYCLOAK}/bin/sep-configure-proxy.cli
replace-env.sh ${SERVICE_DIR_KEYCLOAK}/bin/sep-configure-jms.cli
replace-env.sh ${SERVICE_DIR_KEYCLOAK}/bin/sep-configure-events-listener.cli

${SERVICE_DIR_KEYCLOAK}/bin/jboss-cli.sh --file=${SERVICE_DIR_KEYCLOAK}/bin/sep-configure-datasource.cli
${SERVICE_DIR_KEYCLOAK}/bin/jboss-cli.sh --file=${SERVICE_DIR_KEYCLOAK}/bin/sep-configure-proxy.cli
${SERVICE_DIR_KEYCLOAK}/bin/jboss-cli.sh --file=${SERVICE_DIR_KEYCLOAK}/bin/sep-configure-jms.cli
${SERVICE_DIR_KEYCLOAK}/bin/jboss-cli.sh --file=${SERVICE_DIR_KEYCLOAK}/bin/sep-configure-events-listener.cli

chown -R $DOCKER_USER:$DOCKER_USER ${SERVICE_DIR_KEYCLOAK}

sync