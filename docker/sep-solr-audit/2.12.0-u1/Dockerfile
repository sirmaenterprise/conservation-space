ARG docker_registry=
FROM $docker_registry/solr:5.5.3-u1

ENV DB_USER=sep 		\
	DB_PASSWORD=sep 	\
	DB_NAME=sepaudit 	\
	CMD_ARGS="-Dsolr.audit.template.directory=/opt/solr/audit_config -Dsolr.configset.directory=/var/lib/solr/configsets"

COPY rootfs/ /

ADD ["https://repo1.maven.org/maven2/org/postgresql/postgresql/9.2-1003-jdbc4/postgresql-9.2-1003-jdbc4.jar", "https://repo1.maven.org/maven2/org/apache/solr/solr-dataimporthandler/5.5.3/solr-dataimporthandler-5.5.3.jar", "$SERVICE_DIR_SOLR_HOME/audit_config/lib/"]

RUN chmod +x /docker-init.d/*.sh && \
	chown -R $DOCKER_USER:$DOCKER_USER $SERVICE_DIR_SOLR_HOME

CMD ["sh", "-c", "/opt/solr/bin/solr -f -d /opt/solr/server -a $CMD_ARGS -s $VOLUME_SOLR_DATA $CLI_PARAMS"]
