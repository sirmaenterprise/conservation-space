FROM docker-reg.sirmaplatform.com/openjdk-jdk:8-u1

ARG keycloak_version=4.3.0.Final
ARG keycloak_dl_url=https://downloads.jboss.org/keycloak/$keycloak_version/keycloak-$keycloak_version.tar.gz

ENV KEYCLOAK_USER=$DOCKER_USER \
	KEYCLOAK_PASS=$DOCKER_USER \
	SERVICE_DIR_KEYCLOAK=/opt/keycloak

COPY rootfs/ /

RUN \
	chmod +x /usr/local/bin/healthcheck.sh && \
	curl -fsSL $keycloak_dl_url | tar xz -C /opt && \
	mv ${SERVICE_DIR_KEYCLOAK}-$keycloak_version ${SERVICE_DIR_KEYCLOAK} && \
	mkdir -p "${SERVICE_DIR_KEYCLOAK}/standalone/data/" "${SERVICE_DIR_KEYCLOAK}/standalone/log/" && \
	rm -rf 	${SERVICE_DIR_KEYCLOAK}/README.txt \
			${SERVICE_DIR_KEYCLOAK}/bin/*.bat \
			${SERVICE_DIR_KEYCLOAK}/bin/client/jboss-cli-client.jar \
			${SERVICE_DIR_KEYCLOAK}/bin/client/jboss-client.jar \
			${SERVICE_DIR_KEYCLOAK}/bin/client/README-CLI-JCONSOLE.txt \
			${SERVICE_DIR_KEYCLOAK}/bin/client/README-EJB-JMS.txt \
			${SERVICE_DIR_KEYCLOAK}/docs && \
	chown -R $DOCKER_USER:$DOCKER_USER $SERVICE_DIR_KEYCLOAK

WORKDIR ${SERVICE_DIR_KEYCLOAK}

VOLUME ["${SERVICE_DIR_KEYCLOAK}/standalone/data/", "${SERVICE_DIR_KEYCLOAK}/standalone/log/"]

EXPOSE 8080

HEALTHCHECK --interval=5s --timeout=1s --start-period=15s CMD healthcheck.sh

CMD ["./bin/standalone.sh" , "-b" , "0.0.0.0"]
