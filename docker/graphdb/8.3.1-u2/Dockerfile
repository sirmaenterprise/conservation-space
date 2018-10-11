FROM docker-reg.sirmaplatform.com/openjdk-jre:8

ARG graphdb_version="8.3.1"
ARG graphdb_dl_url="http://maven.ontotext.com/content/groups/all-onto/com/ontotext/graphdb/graphdb-ee/$graphdb_version/graphdb-ee-$graphdb_version-dist.zip"
ARG graphdb_dl_path="/tmp/graphdb-ee-$graphdb_version"

ENV GDB_HEAP_SIZE="2g" \
	VOLUME_REPOS="/var/lib/graphdb" \
	SERVICE_DIR_GRAPHDB_HOME="/opt/graphdb" \
	GDB_GC_LOG_FILE="/opt/graphdb/logs" \
	GDB_HEAP_DUMP_FILE="/opt/graphdb/logs/" \
	GDB_GC_LOG="true"

COPY rootfs/ /

RUN \
	apk --no-cache add curl bash rsync && \
	mkdir -p /var/lib/graphdb "$SERVICE_DIR_GRAPHDB_HOME/logs" && \
	wget -q -O /tmp/graphdb.zip $graphdb_dl_url && \
	unzip /tmp/graphdb.zip -d /tmp && \
	rsync -r \
		--exclude examples \
		--exclude benchmark \
		--exclude tools \
		--exclude adapters \
		$graphdb_dl_path/ $SERVICE_DIR_GRAPHDB_HOME/ && \
	chown -R $DOCKER_USER:$DOCKER_USER $SERVICE_DIR_GRAPHDB_HOME /var/lib/graphdb && \
	chmod +x $SERVICE_DIR_GRAPHDB_HOME/bin/* /docker-init.d/*.sh /usr/sbin/healthcheck.sh && \
	apk --no-cache del rsync && \
	rm -rf /tmp/*

VOLUME ["/var/lib/graphdb", "/opt/graphdb/logs"]

WORKDIR ${SERVICE_DIR_GRAPHDB_HOME}

HEALTHCHECK --interval=10s --timeout=3s CMD healthcheck.sh

EXPOSE 8080 8091

CMD ["./bin/startGraphdb.sh"]
