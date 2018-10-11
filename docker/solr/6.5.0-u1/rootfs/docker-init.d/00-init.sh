#!/bin/sh

sed -i "s/log4j.rootLogger=INFO/log4j.rootLogger=$LOG_LEVEL/" "$SERVICE_DIR_SOLR_HOME/server/resources/log4j.properties"

if [ ! -f $VOLUME_SOLR_DATA/solr.xml ]; then

	cp -r $SERVICE_DIR_SOLR_HOME/server/solr/* $VOLUME_SOLR_DATA
	chown -R $DOCKER_USER:$DOCKER_USER $VOLUME_SOLR_DATA
fi

sync
