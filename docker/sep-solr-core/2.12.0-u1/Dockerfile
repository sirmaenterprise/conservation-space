FROM docker-reg.sirmaplatform.com/solr:6.5.0-u1

ENV SEP_VERSION="2.12.0"

COPY rootfs/ /

RUN \
	apk --no-cache add curl && \
	rm -rf $VOLUME_SOLR_DATA/* && \
	cp -R $SERVICE_DIR_SOLR_HOME/solr-init-data/* $VOLUME_SOLR_DATA && \
	chown -R $DOCKER_USER:$DOCKER_USER $SERVICE_DIR_SOLR_HOME
