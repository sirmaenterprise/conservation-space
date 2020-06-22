FROM node:8

ENV \
	EXPORT_OUTPUT_DIR="/opt/export/output" \
	EXPORT_LOG_FILE="/opt/export/logs/export.json" \
	EXPORT_LOG_LEVEL="info" \
	EXPORT_OUTPUT_CLEANER_AGE="60"

RUN \
	apt-get update && \
	apt-get install -yq \
		gconf-service \
		libasound2 \
		libatk1.0-0 \
		libc6 \
		libcairo2 \
		libcups2 \
		libdbus-1-3 \
		libexpat1 \
		libfontconfig1 \
		libgcc1 \
		libgconf-2-4 \
		libgdk-pixbuf2.0-0 \
		libglib2.0-0 \
		libgtk-3-0 \
		libnspr4 \
		libpango-1.0-0 \
		libpangocairo-1.0-0 \
		libstdc++6 \
		libx11-6 \
		libx11-xcb1 \
		libxcb1 \
		libxcomposite1 \
		libxcursor1 \
		libxdamage1 \
		libxext6 \
		libxfixes3 \
		libxi6 \
		libxrandr2 \
		libxrender1 \
		libxss1 \
		libxtst6 \
		fonts-ipafont-gothic \
		fonts-wqy-zenhei \
		fonts-thai-tlwg \
		fonts-kacst ttf-freefont \
		ca-certificates \
		fonts-liberation \
		libappindicator1 \
		libnss3 \
		lsb-release \
		xdg-utils \
		cron && \
		apt-get clean && \
		apt-get autoremove -y && \
		rm -rf /var/lib/apt/lists/*

COPY rootfs/ /
COPY package.json index.js /opt/export/
COPY lib /opt/export/lib

WORKDIR /opt/export

RUN \
	npm --progress=false --production install && \
	npm cache clean --force && \
	chmod +x /etc/cron.hourly/export_cleanup /usr/local/bin/*.sh /entrypoint.sh && \
	mkdir "$EXPORT_OUTPUT_DIR" /opt/export/logs && \
	chown -R $DOCKER_USER:$DOCKER_USER /opt/export

EXPOSE 8080

VOLUME ["/opt/export/output", "/opt/export/logs"]

HEALTHCHECK CMD /usr/local/bin/healthcheck.sh

ENTRYPOINT ["/entrypoint.sh"]
CMD ["node", "/opt/export/index.js"]
