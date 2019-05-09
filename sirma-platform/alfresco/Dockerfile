FROM docker-reg.sirmaplatform.com/alfresco:4.2.c-u3

ENV DB_USER="sep" \
	DB_USER_PASS="sep" \
	IDP_HOST="idp" \
	IDP_PORT="9443" \
	SSO_ENABLED="true" \
	LDAP_HOST="idp" \
	LDAP_PORT="10389" \
	LDAP_USERNAME="uid\=admin,ou\=system" \
	LDAP_PASSWORD="admin" \
	LDAP_AUTH_TIMEOUT="5000"

COPY docker-rootfs/ /
COPY alfresco-emf-integration/alfresco/target/alfresco.war /opt/tomcat/webapps/

RUN \
	apk --no-cache add curl xvfb ttf-freefont fontconfig dbus zip && \
	apk --no-cache \
		--repository http://dl-cdn.alpinelinux.org/alpine/edge/main \
		--repository http://dl-cdn.alpinelinux.org/alpine/edge/community \
		--allow-untrusted add wkhtmltopdf && \
	unzip webapps/alfresco.war -d webapps/ && \
	rm -rf webapps/alfresco.war webapps/WEB-INF/classes/alfresco/module/backup && \
	sed -i 's|log4j.appender.File.File=|log4j.appender.File.File=/opt/tomcat/logs/|' webapps/WEB-INF/classes/log4j.properties && \
	(cd webapps; zip -r ../alfresco.war .) && \
	rm -rf webapps/* && \
	mv alfresco.war webapps/ && \
	mv /usr/bin/wkhtmltopdf /usr/bin/wkhtmltopdf-origin && \
    chown -R $DOCKER_USER:$DOCKER_USER /opt/tomcat && \
	chmod +x /docker-init.d/*.sh /usr/local/sbin/wkhtmltopdf /usr/local/sbin/healthcheck.sh && \
	apk --no-cache del zip

HEALTHCHECK --start-period=10m CMD healthcheck.sh
