FROM docker-reg.sirmaplatform.com/openjdk-jre:8-1

ARG wildfly_home_dir="/opt/wildfly"

ENV JBOSS_HOME="$wildfly_home_dir" \
	SERVICE_DIR_WILDFLY_HOME="$wildfly_home_dir" \
	VOLUME_WILDFLY_LOGS="$wildfly_home_dir/standalone/log" \
	LOG_LEVEL="INFO"

COPY rootfs/ /

RUN \
	apk --no-cache add tar && \
	mkdir -p $wildfly_home_dir && \
	curl http://download.jboss.org/wildfly/9.0.2.Final/wildfly-9.0.2.Final.tar.gz | tar -xz --strip-components=1 -C $wildfly_home_dir && \
	rm -rf $wildfly_home_dir/README.txt \
			$wildfly_home_dir/docs \
			$wildfly_home_dir/welcome-content \
			$wildfly_home_dir/bin/client \
			$wildfly_home_dir/bin/init.d \
			$wildfly_home_dir/bin/service \
			$wildfly_home_dir/bin/*.bat &&\
	mkdir -p $wildfly_home_dir/standalone/log && \
	mkdir -p $wildfly_home_dir/standalone/deployments && \
	mkdir -p $wildfly_home_dir/standalone/data && \
	chmod +x /docker-init.d/*.sh && \
	chown -R $DOCKER_USER:$DOCKER_USER $wildfly_home_dir && \
	apk --no-cache del tar

WORKDIR $wildfly_home_dir

VOLUME ["$wildfly_home_dir/standalone/log", "$wildfly_home_dir/standalone/data"]

EXPOSE 8080 9990 8787

CMD ["/bin/sh", "-c", "./bin/standalone.sh -b 0.0.0.0 $EXTRA_CLI_ARGS"]
