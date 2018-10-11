FROM docker-reg.sirmaplatform.com/base:2

ENV CHROMIUM_PATH="/usr/bin/chromium-browser" \
	EXPORT_OUTPUT_DIR="/opt/export/output" \
	EXPORT_OUTPUT_CLEANER_AGE=60

COPY rootfs/ /
COPY package.json index.js /opt/export/
COPY lib /opt/export/lib

WORKDIR /opt/export

RUN \
	apk --no-cache --repository http://dl-cdn.alpinelinux.org/alpine/v3.8/main add \
		nodejs=8.11.4-r0 \
		nodejs-npm=8.11.4-r0 \
		freetype=2.9.1-r1 \
		harfbuzz=1.7.6-r1 \
		nss=3.36.1-r1 && \
	# not only they bumped chromium four versions up, but they also seem to have broke it's dependencies
	# https://github.com/GoogleChrome/puppeteer/issues/3019
	apk --no-cache --repository http://dl-cdn.alpinelinux.org/alpine/v3.8/community add \
		chromium=68.0.3440.75-r0 && \
	PUPPETEER_SKIP_CHROMIUM_DOWNLOAD=true npm --progress=false --production install && \
	npm cache clean --force && \
	apk --no-cache del nodejs-npm && \
	chmod +x /etc/periodic/15min/export_cleanup /docker-init.d/*.sh /usr/local/bin/*.sh && \
	mkdir "$EXPORT_OUTPUT_DIR" && \
	chown -R $DOCKER_USER:$DOCKER_USER /opt/export

EXPOSE 8080

VOLUME /opt/export/output

WORKDIR /opt/export

HEALTHCHECK CMD /usr/local/bin/healthcheck.sh

CMD ["node", "/opt/export/index.js"]
