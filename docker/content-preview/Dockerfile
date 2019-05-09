FROM docker-reg.sirmaplatform.com/libreoffice:6.1-u1

ARG DEBIAN_FRONTEND=noninteractive

ENV JMS_HOST="wildfly" \
    JMS_PORT="5445" \
    JMS_USER="guest" \
    JMS_PASS="guest" \
    JMS_CONCURRENCY=1 \
    PREVIEW_TIMEOUT=300000 \
    PREVIEW_TMP=/tmp/preview \
    PREVIEW_LOGS=/var/log/preview \
    PREVIEW_LOG_LEVEL=warn

COPY rootfs/ /

COPY target/content-preview.jar .

RUN \
    apt-get update && \
    apt-get install -y --no-install-recommends curl locales ffmpeg && \
    locale-gen en_US.UTF-8 && \
    update-locale LANG=en_US.UTF-8 && \
    dpkg-reconfigure locales && \
    chmod +x /usr/local/bin/*.sh && \
    mkdir -p $PREVIEW_TMP $PREVIEW_LOGS && \
    rm -rf /tmp/* /var/lib/apt/lists/*

ENV LANG=en_US.UTF-8

VOLUME ["${PREVIEW_LOGS}"]

EXPOSE 8300

HEALTHCHECK --interval=10s --timeout=3s --retries=3 --start-period=10s CMD preview-healthcheck.sh

CMD ["start-preview-service.sh"]
