#!/usr/bin/env sh

replace-env.sh /opt/tomcat/shared/classes/alfresco-global.properties

if [ ! -d /var/lib/alfresco/keystore ]; then
	cp -R /opt/tomcat/solr/alf_data/keystore/ /var/lib/alfresco/
fi

