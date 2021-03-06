ARG docker_registry=
FROM $docker_registry/wso2idp:5

ENV ADMIN_USERNAME="admin" \
	ADMIN_PASSWORD="admin" \
	TENANT_ADMIN_ADD="true" \
	TENANT_ADMIN_USERNAME="systemadmin" \
	TENANT_ADMIN_PASSWORD="admin" \
	DB_USER="sep" \
	DB_USER_PASS="sep" \
	DB_NAME="wso2is" \
	KEYSTORE_FILENAME="wso2carbon.jks" \
	KEYSTORE_ALIAS="wso2carbon" \
	KEYSTORE_PASS="wso2carbon" \
	TRUSTSTORE_FILENAME="client-truststore.jks" \
	TRUSTSTORE_PASS="wso2carbon" \
	LDAP_USE_EMBEDDED="true" \
	LDAP_HOST="localhost" \
	LDAP_PORT="10389" \
	LDAP_USERNAME="uid=admin,ou=system" \
	LDAP_PASSWORD="admin" \
	MAIL_FROM="itt-test@sirma.bg" \
	MAIL_USER="sepadmin" \
	MAIL_PASS="" \
	MAIL_HOST="mcatcher.sirmaplatform.com" \
	MAIL_PORT="2525" \
	MAIL_START_TLS="false" \
	MAIL_AUTH="false" \
	NOTIFICATION_EXPIRE_TIME="1440" \
	MAX_ROLE_LIST_LENGTH=1000 \
	MAX_USER_LIST_LENGTH=1000 \
	MIN_PASS_LENGTH=6 \
	MAX_PASS_LENGTH=30 \
	VOLUME_WSOIDP_KEYSTORE="/opt/wso2is/repository/resources/keystore"

COPY patch0009/ /opt/wso2is/repository/components/patches/patch0009/

RUN \
	apk --no-cache add unzip && \
	mkdir /opt/wso2is/repository/data-dir-init && \
	curl -sfL --referer "http://connect.wso2.com/wso2/getform/reg/new_product_download" https://product-dist.wso2.com/products/identity-server/5.0.0/service-pack/WSO2-IS-5.0.0-SP01.zip > /opt/sp01.zip && \
	(cd /opt; unzip sp01.zip) && \
	ln -s /opt/wso2is /opt/wso2is-5.0.0 && \
	chmod +x /opt/WSO2-IS-5.0.0-SP01/install_sp.sh && \
	echo "y" > /opt/yes_install_wso_service_pack && \
	(cd /opt/WSO2-IS-5.0.0-SP01; sh /opt/WSO2-IS-5.0.0-SP01/install_sp.sh < /opt/yes_install_wso_service_pack) && \
	mv /opt/wso2is/repository/data/is-default-schema.zip /opt/wso2is/repository/data-dir-init/is-default-schema.zip && \
	mkdir /opt/wso2is/repository/deployment/server/webapps/authenticationendpoint/ && \
	unzip /opt/wso2is/repository/deployment/server/webapps/authenticationendpoint.war -d /opt/wso2is/repository/deployment/server/webapps/authenticationendpoint/ && \
	rm -f /opt/wso2is/repository/deployment/server/webapps/authenticationendpoint.war && \
	chown -R $DOCKER_USER:$DOCKER_USER /opt/wso2is && \
	apk --no-cache del unzip && \
	rm -rf /opt/wso2is-5.0.0 /opt/WSO2-IS-5.0.0-SP01 /opt/yes_install_wso_service_pack

COPY rootfs/ /

VOLUME /opt/wso2is/repository/resources/keystore

HEALTHCHECK --start-period=15m CMD healthcheck.sh
