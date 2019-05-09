FROM docker-reg.sirmaplatform.com/keycloak:4.3.0-u1

ARG jdbc_postgres_version=42.2.2
ARG jdbc_postgres_url=http://central.maven.org/maven2/org/postgresql/postgresql/$jdbc_postgres_version/postgresql-$jdbc_postgres_version.jar

ENV \
	HOST_NAME=keycloak \
	KEYCLOAK_USER=systemadmin \
	KEYCLOAK_PASS=admin \
	DB_HOST=db \
	DB_PORT=5432 \
	DB_NAME=keycloak \
	DB_USER=sep \
	DB_PASS=sep \
	DB_VALIDATION=true \
	DB_VALIDATION_MS=60000 \
	DB_FLUSH_STRATEGY=IdleConnections \
	PROXY_ADDRESS_FORWARDING=true \
	PROXY_HTTPS_PORT=443 \
	MASTER_DISPLAY_NAME="Sirma Enterprise Platform" \
	MASTER_DISPLAY_NAME_HTML="Sirma Enterprise Platform" \
	THEME_NAME=sep \
	SEP_UI_ADDR=http://ui \
	JMS_HOST=wildfly \
	JMS_PORT=5445 \
	JMS_REDELIVERY_DELAY=60000 \
	JMS_MAX_DELIVERY_ATTEMPTS=30 \
	JMS_INCLUDE_EVENTS="LOGIN,LOGOUT,UPDATE_PASSWORD" \
	JMS_INCLUDE_CLIENTS="sep-ui"

COPY rootfs/ /

COPY jms-events-listener/target/jms-events-listener.jar ${SERVICE_DIR_KEYCLOAK}/standalone/deployments/
COPY ldap-provider-extension/target/ldap-provider-extension.jar ${SERVICE_DIR_KEYCLOAK}/standalone/deployments/
COPY db-patch/target/db-patch.jar ${SERVICE_DIR_KEYCLOAK}/standalone/deployments/
COPY sep-authenticator/target/sep-authenticator.jar ${SERVICE_DIR_KEYCLOAK}/standalone/deployments/

ADD $jdbc_postgres_url ${SERVICE_DIR_KEYCLOAK}/modules/system/layers/base/org/postgresql/jdbc/main/postgresql.jar

HEALTHCHECK --start-period=50s CMD healthcheck.sh
