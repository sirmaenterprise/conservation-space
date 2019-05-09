FROM docker-reg.sirmaplatform.com/base:2

ENV \
	COMPARE_OUTPUT_DIR="/opt/compare-pdf/output" \
	COMPARE_LOG_LEVEL="info" \
	COMPARE_LOG_FILE="/opt/compare-pdf/logs/compare.json" \
	COMPARE_OUTPUT_CLEANER_AGE="60"

COPY rootfs/ /
COPY ["lib/", "/opt/compare-pdf/lib"]
COPY ["package.json", "index.js", "/opt/compare-pdf/"]

WORKDIR /opt/compare-pdf

RUN \
    apk add --no-cache xvfb xdpyinfo nodejs nodejs-npm && \
    apk add --no-cache ttf-freefont fontconfig dbus && \
    apk add --no-cache poppler qt poppler-qt4 icu-libs build-base gcc qt-dev poppler-qt4-dev && \
    curl -L https://github.com/taurus-forever/diffpdf-console/archive/master.tar.gz | tar -zx -C /tmp && \
    (cd /tmp/diffpdf-console-master; lrelease diffpdf.pro; qmake; make) && \
    cp /tmp/diffpdf-console-master/diffpdf /usr/local/bin && \
    chmod +x /usr/local/bin/* /docker-init.d/*.sh /etc/periodic/15min/* && \
    npm install --progress=false --production && \
    npm cache clear --force && \
    mkdir -p /opt/compare-pdf/output /opt/compare-pdf/logs /root && \
    chown -R $DOCKER_USER:$DOCKER_USER /opt/compare-pdf && \
    apk --no-cache del build-base gcc qt-dev poppler-qt4-dev nodejs-npm && \
    rm -rf /tmp/*

VOLUME ["/opt/compare-pdf/output", "/opt/compare-pdf/logs"]

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s CMD healthcheck.sh

CMD ["node", "index.js"]
