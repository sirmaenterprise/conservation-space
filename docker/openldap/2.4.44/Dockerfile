FROM docker-reg.sirmaplatform.com/base:2

ENV VOLUME_LDAP_DATA=/var/lib/openldap \
	VOLUME_LDAP_LOGS=/var/log/openldap \
	SERVICE_DIR_LDAP_SCHEMAS=/etc/openldap \
	SERVICE_DIR_LDAP_RUN=/var/run/openldap \
	LDAP_CONFIG_DIR=/var/lib/openldap/slapd.d \
	LDAP_DEBUG_LEVEL=256

RUN apk add --no-cache openldap=2.4.44-r5 openldap-clients rsyslog logrotate && \
	rm -f /etc/openldap/*.conf* /etc/openldap/*.ldif* /etc/openldap/*.example* && \
	rm -rf $VOLUME_LDAP_DATA/openldap-data/*

COPY rootfs/ /

RUN chmod +x /usr/local/bin/*.sh && \
	mkdir -p $VOLUME_LDAP_LOGS $LDAP_CONFIG_DIR && \
	chown -R $DOCKER_USER:$DOCKER_USER $SERVICE_DIR_LDAP_SCHEMAS $VOLUME_LDAP_DATA $VOLUME_LDAP_LOGS $SERVICE_DIR_LDAP_RUN

VOLUME ["$VOLUME_LDAP_DATA", "$VOLUME_LDAP_LOGS"]

EXPOSE 10389

HEALTHCHECK --interval=5s --timeout=1s --start-period=5s CMD healthcheck.sh

# Killing slapd by a more drastic method may cause information loss or database corruption
STOPSIGNAL INT

CMD start-openldap.sh
