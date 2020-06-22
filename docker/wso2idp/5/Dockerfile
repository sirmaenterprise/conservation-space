ARG docker_registry=
FROM $docker_registry/openjdk-jre:7-base-1

RUN \
	apk --no-cache add unzip && \
	curl -f 'https://product-dist.wso2.com/products/identity-server/5.0.0/wso2is-5.0.0.zip' -H 'Referer: https://wso2.com/identity-and-access-management/previous-releases/' --compressed > /opt/wso2is-5.0.0.zip && \
	unzip /opt/wso2is-5.0.0.zip -d /opt && \
	mv /opt/wso2is-5.0.0 /opt/wso2is && \
	rm -f /opt/wso2is-5.0.0.zip && \
	apk --no-cache del unzip

ENV SERVICE_DIR_WSO2IS_HOME="/opt/wso2is" \
	VOLUME_WSOIDP_LOGS="/opt/wso2is/repository/logs" \
	VOLUME_WSOIDP_DATA="/opt/wso2is/repository/data"

COPY rootfs/ /

RUN \
	chmod +x /usr/sbin/healthcheck.sh && \
	mkdir -p $VOLUME_WSOIDP_DATA $VOLUME_WSOIDP_LOGS && \
	chown -R $DOCKER_USER:$DOCKER_USER /opt/wso2is

WORKDIR /opt/wso2is

EXPOSE 9763 9443 10389

VOLUME ["/opt/wso2is/repository/logs" , "/opt/wso2is/repository/data"]

HEALTHCHECK --interval=10s --timeout=3s CMD healthcheck.sh

CMD ["./bin/wso2server.sh", "-Dsetup"]
