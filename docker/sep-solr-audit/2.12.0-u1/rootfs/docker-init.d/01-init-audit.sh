#!/usr/bin/env sh

AUDIT_CFGSET="$VOLUME_SOLR_DATA/configsets/audit_config"

if [ -d $AUDIT_CFGSET ]; then
	rm -rf $VOLUME_SOLR_DATA/configsets/audit_config/*
else
	mkdir -p $AUDIT_CFGSET
fi

replace-env.sh $SERVICE_DIR_SOLR_HOME/audit_config/conf/data-config.xml

cp -R $SERVICE_DIR_SOLR_HOME/audit_config/* $VOLUME_SOLR_DATA/configsets/audit_config

chown -R $DOCKER_USER:$DOCKER_USER $VOLUME_SOLR_DATA/configsets
sync
