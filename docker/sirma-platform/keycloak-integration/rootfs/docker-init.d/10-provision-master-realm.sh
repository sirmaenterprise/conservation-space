#!/usr/bin/env bash

set -eu

function provision() {
    wait-for-service.sh -h 127.0.0.1 -p 8080 &>/dev/null

    # authenticate
    ${SERVICE_DIR_KEYCLOAK}/bin/kcadm.sh config credentials --server http://127.0.0.1:8080/auth --realm master --user ${KEYCLOAK_USER} --password ${KEYCLOAK_PASS}

    updateRealmProperties

    createSepUiClient

    updateAdminConsoleClient

    checkIfNeedsProvision || createAuthFlow
}

function createSepUiClient() {
    RESULT=`${SERVICE_DIR_KEYCLOAK}/bin/kcadm.sh get clients --fields=clientId --format 'csv' --noquotes`
    if [[ $RESULT != *"sep-ui"* ]]; then
        replace-env.sh /docker-init.d/10-sep-ui-client.json
        ${SERVICE_DIR_KEYCLOAK}/bin/kcadm.sh create clients -f /docker-init.d/10-sep-ui-client.json
    fi
}

function updateAdminConsoleClient() {
    CONSOLE_CLIENT_ID=`${SERVICE_DIR_KEYCLOAK}/bin/kcadm.sh get clients -q clientId=security-admin-console --fields=id --format 'csv' --noquotes`

    ${SERVICE_DIR_KEYCLOAK}/bin/kcadm.sh update clients/$CONSOLE_CLIENT_ID -s 'redirectUris=["/auth/admin/master/console/*","'${SEP_UI_ADDR}'*"]'

    ${SERVICE_DIR_KEYCLOAK}/bin/kcadm.sh create clients/$CONSOLE_CLIENT_ID/protocol-mappers/models -f /docker-init.d/10-tenant-mapper.json
}

function checkIfNeedsProvision() {
    FLOW_NAME="$(${SERVICE_DIR_KEYCLOAK}/bin/kcadm.sh get realms/master --format 'csv' --noquotes --fields browserFlow)"
    if [ $FLOW_NAME != "sep-auth-flow" ]; then
        return 1;
    fi
}

function updateRealmProperties() {
    ${SERVICE_DIR_KEYCLOAK}/bin/kcadm.sh update realms/master -s displayName="${MASTER_DISPLAY_NAME}" -s displayNameHtml="${MASTER_DISPLAY_NAME_HTML}" -s loginTheme=${THEME_NAME} -s emailTheme=${THEME_NAME} -s loginWithEmailAllowed=false
}

function createAuthFlow() {
    # create parent flow
    SEP_FLOW_ID=`${SERVICE_DIR_KEYCLOAK}/bin/kcadm.sh create authentication/flows -i -s alias="sep-auth-flow" -s providerId="basic-flow" -s topLevel=true -s builtIn=false`

    # add cookie authenticator to parent flow
    ${SERVICE_DIR_KEYCLOAK}/bin/kcadm.sh create authentication/executions -s authenticator="auth-cookie" -s parentFlow="$SEP_FLOW_ID" -s priority=0 -s requirement=ALTERNATIVE

    # create sub flow in parent flow
    SEP_SUB_FLOW_ID=`${SERVICE_DIR_KEYCLOAK}/bin/kcadm.sh create "authentication/flows/sep-auth-flow/executions/flow" -i -s alias="sep-sub-flow" -s type="basic-flow"`

    # set requirement to alternative for sub flow and cookie authenticator
    EXECUTION_IDS=`${SERVICE_DIR_KEYCLOAK}/bin/kcadm.sh get authentication/flows/sep-auth-flow/executions -c | grep -o '"id": *"[^"]*"' | grep -o '"[^"]*"$' | grep -o '[^"]*'`
    for ID in $EXECUTION_IDS
    do
      ${SERVICE_DIR_KEYCLOAK}/bin/kcadm.sh update "authentication/flows/sep-auth-flow/executions" -b "{\"id\" : \"$ID\",\"requirement\" : \"ALTERNATIVE\"}"
    done

    # add sep realm authenticator to the sub flow
    ${SERVICE_DIR_KEYCLOAK}/bin/kcadm.sh create authentication/executions -s authenticator="sep-realm-based-authenticator" -s parentFlow="$SEP_SUB_FLOW_ID" -s priority=0 -s requirement=REQUIRED

    # add sep username/password authenticator to the sub flow
    ${SERVICE_DIR_KEYCLOAK}/bin/kcadm.sh create authentication/executions -s authenticator="sep-authenticator" -s parentFlow="$SEP_SUB_FLOW_ID" -s priority=10 -s requirement=REQUIRED

    # update master realm to use the new flow
    ${SERVICE_DIR_KEYCLOAK}/bin/kcadm.sh update realms/master -s browserFlow="sep-auth-flow"

    echo "=================== PROVISIONED MASTER REALM ==================="
}

# Schedule the provisioning after Keycloak is up & running
provision &