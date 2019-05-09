FROM alpine:3.8

ENV \
	LANG=en_US.utf8 \
	DOCKER_USER=dockeru \
	DOCKER_USER_ID=1000 \
	DOCKER_USER_GROUP_ID=1000

COPY rootfs/ /

RUN \
	apk --no-cache add curl bash shadow && \
	mkdir /opt /docker-init.d && \
	addgroup -g $DOCKER_USER_GROUP_ID $DOCKER_USER && \
	adduser $DOCKER_USER -u $DOCKER_USER_ID -S -G $DOCKER_USER -s /bin/bash && \
	sed -i -e 's|/bin/sh|/bin/bash|' -e 's|/bin/ash|/bin/bash|' /etc/passwd && \
	curl -o /usr/local/bin/gosu -fSL "https://github.com/tianon/gosu/releases/download/1.11/gosu-amd64" && \
	chmod +x /entrypoint.sh /usr/local/bin/gosu /usr/local/bin/*.sh

ENTRYPOINT ["/entrypoint.sh"]
