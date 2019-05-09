FROM docker-reg.sirmaplatform.com/openjdk-jdk:7

ENV VOLUME_ALF_DATA="/var/lib/alfresco" \
	VOLUME_SOLR_DATA="/var/lib/alfresco-solr" \
	SERVICE_DIR_ALF_HOME="/opt/tomcat" \
	DB_HOST="db" \
	DB_PORT="5432" \
	DB_USER="postgres" \
	DB_USER_PASS="postgres" \
	DB_NAME="alfresco" \
	DB_POOL_VALIDATE_ON_BORROW="true" \
	DB_POOL_EVICT_IDLE_MIN="600000" \
	DB_POOL_EVICT_INTERVAL="180000"

COPY rootfs/ /

RUN \
	chmod +x /docker-init.d/*.sh /usr/local/sbin/*.sh /opt/tomcat/bin/*.sh && \
	apk --no-cache add giflib-dev libjpeg-turbo-dev libart-lgpl libreoffice unzip freetype-dev imagemagick ghostscript ghostscript-fonts patch wget zip && \
	mkdir /tmp/alfresco /tmp/solr && \
	wget -q http://dl.alfresco.com/release/community/build-04576/alfresco-community-4.2.c.zip -P /tmp && \
	wget -q https://archive.apache.org/dist/tomcat/tomcat-6/v6.0.45/bin/apache-tomcat-6.0.45.tar.gz -P /tmp && \
	wget -q http://dl.alfresco.com/release/community/build-04576/alfresco-community-solr-4.2.c.zip -P /tmp && \
	(cd /tmp; tar -zxf apache-tomcat-6.0.45.tar.gz; unzip alfresco-community-4.2.c.zip -d /tmp/alfresco) && \
	cp -R /tmp/apache-tomcat-6.0.45/* /opt/tomcat/ && \
	sed -i 's/shared.loader=/shared.loader=${catalina.base}\/shared\/classes\//' /opt/tomcat/conf/catalina.properties && \
	rm -rf /opt/tomcat/webapps/* && \
	mv /tmp/alfresco/bin/* /opt/tomcat/bin && \
	mv /tmp/alfresco/web-server/lib/* /opt/tomcat/lib && \
	mv -n /tmp/alfresco/web-server/endorsed /tmp/alfresco/web-server/shared/* /opt/tomcat/shared && \
	mkdir /opt/tomcat/solr /var/lib/alfresco-solr /var/lib/alfresco && \
	chown -R $DOCKER_USER:$DOCKER_USER $VOLUME_ALF_DATA $VOLUME_SOLR_DATA && \
	(cd /tmp; unzip alfresco-community-solr-4.2.c.zip -d /opt/tomcat/solr) && \
	mkdir /opt/tomcat/solr/configure-logging && \
	cd /opt/tomcat/solr/configure-logging && \
	unzip ../apache-solr-1.4.1.war -d ./ && \
	sed -i 's|log4j.appender.File.File=|log4j.appender.File.File=/opt/tomcat/logs/|' WEB-INF/classes/log4j.properties && \
	rm -f ../apache-solr-1.4.1.war && \
	zip -r ../apache-solr-1.4.1.war . && \
	cd .. && \
	rm -rf configure-logging apache-solr-1.4.1.war.unpatched && \
	cd / && \
	mkdir -p /opt/tomcat/conf/Catalina/localhost && \
	cp /opt/tomcat/solr/solr-tomcat-context.xml /opt/tomcat/conf/Catalina/localhost/solr.xml && \
	sed -i 's/@@ALFRESCO_SOLR_DIR@@/\/opt\/tomcat\/solr/' /opt/tomcat/conf/Catalina/localhost/solr.xml && \
	sed -i 's/@@ALFRESCO_SOLR_DIR@@/\/var\/lib\/alfresco-solr\//' /opt/tomcat/solr/workspace-SpacesStore/conf/solrcore.properties && \
	sed -i 's/@@ALFRESCO_SOLR_DIR@@/\/var\/lib\/alfresco-solr\//' /opt/tomcat/solr/archive-SpacesStore/conf/solrcore.properties && \
	(cd /opt/tomcat/conf; patch -l server.xml server.xml.patch; patch -l tomcat-users.xml tomcat-users.xml.patch) && \
	chown -R $DOCKER_USER:$DOCKER_USER /opt/tomcat && \
	apk --no-cache del unzip patch wget zip && \
	rm -rf /tmp/*

EXPOSE 8080
WORKDIR /opt/tomcat

VOLUME ["/var/lib/alfresco", "/var/lib/alfresco-solr"]

CMD ["./bin/alfresco-start.sh"]
