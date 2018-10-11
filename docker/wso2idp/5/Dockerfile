FROM docker-reg.sirmaplatform.com/openjdk-jre:7-base-1

ENV SERVICE_DIR_WSO2IS_HOME="/opt/wso2is" \
	VOLUME_WSOIDP_LOGS="/opt/wso2is/repository/logs" \
	VOLUME_WSOIDP_DATA="/opt/wso2is/repository/data"

COPY rootfs/ /

RUN \
	apk --no-cache add unzip && \
	chmod +x /usr/sbin/healthcheck.sh && \
	curl -sfL --referer "http://connect.wso2.com/wso2/getform/reg/new_product_download" http://dist.wso2.org/products/identity-server/5.0.0/wso2is-5.0.0.zip > /opt/wso2is.zip && \
	(cd /opt; unzip wso2is.zip) && \
	rm -f /opt/wso2is.zip && \
	mv /opt/wso2is-5.0.0 /opt/wso2is && \
	mkdir -p $VOLUME_WSOIDP_DATA $VOLUME_WSOIDP_LOGS && \
	chown -R $DOCKER_USER:$DOCKER_USER /opt/wso2is && \
	apk --no-cache del unzip

WORKDIR /opt/wso2is

EXPOSE 9763 9443 10389

VOLUME ["/opt/wso2is/repository/logs" , "/opt/wso2is/repository/data"]

HEALTHCHECK --interval=10s --timeout=3s CMD healthcheck.sh

CMD ["./bin/wso2server.sh", "-Dsetup"]
