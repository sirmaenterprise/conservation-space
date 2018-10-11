FROM alpine:3.4

COPY rootfs/ /
COPY ["package.json", "src/", "/opt/compare-pdf/"]

WORKDIR /opt/compare-pdf

RUN \
    apk add --no-cache curl xvfb xdpyinfo nodejs && \
    apk add --no-cache ttf-freefont fontconfig dbus && \
    npm install --progress=false --production && \
    npm cache clear && \
    apk add --no-cache poppler qt poppler-qt4 icu-libs build-base gcc qt-dev poppler-qt4-dev && \
    curl -L https://github.com/taurus-forever/diffpdf-console/archive/master.tar.gz | tar -zx -C /tmp && \
    (cd /tmp/diffpdf-console-master; lrelease diffpdf.pro; qmake; make) && \
    cp /tmp/diffpdf-console-master/diffpdf /usr/local/bin && \
    chmod +x /usr/local/bin/* && \
    # Removing any build dependencies
    apk del build-base gcc qt-dev poppler-qt4-dev && \
    rm -rf /tmp/*

EXPOSE 8125

HEALTHCHECK --interval=30s --timeout=10s CMD healthcheck.sh

ENTRYPOINT ["startup.sh"]
