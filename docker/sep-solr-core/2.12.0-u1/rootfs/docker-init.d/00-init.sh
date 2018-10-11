#!/usr/bin/env sh

if [ "x`cat $VOLUME_SOLR_DATA/solr.xml | grep GraphDBConnectorAdminHandler`" = "x" ] || [ ! -f "$VOLUME_SOLR_DATA/migrated" ] || [ ! "$(grep $SEP_VERSION -s $VOLUME_SOLR_DATA/migrated)" = "$SEP_VERSION" ]; then
		echo '!!! REMOVING ALL SOLR CORES !!!'
        rm -rf $VOLUME_SOLR_DATA/*
        cp -r $SERVICE_DIR_SOLR_HOME/solr-init-data/* $VOLUME_SOLR_DATA/
        echo "$SEP_VERSION" >>  "$VOLUME_SOLR_DATA/migrated"
fi

chown -R $DOCKER_USER:$DOCKER_USER $VOLUME_SOLR_DATA/ $SERVICE_DIR_SOLR_HOME/
