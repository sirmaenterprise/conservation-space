FROM alpine:3.6

ENV LANG=en_US.utf8 \
	DOCKER_USER=dockeru

COPY rootfs/ /

RUN apk --no-cache add curl bash && \
	mkdir /opt /docker-init.d && \
	addgroup $DOCKER_USER && \
	adduser $DOCKER_USER -S -G $DOCKER_USER -s /bin/bash && \
	sed -i -e 's|/bin/sh|/bin/bash|' -e 's|/bin/ash|/bin/bash|' /etc/passwd && \
	curl -o /usr/local/bin/gosu -fsSL "https://github.com/tianon/gosu/releases/download/1.9/gosu-amd64" && \
	chmod +x /entrypoint.sh /usr/local/bin/gosu /usr/local/bin/*.sh

ENTRYPOINT ["/entrypoint.sh"]
