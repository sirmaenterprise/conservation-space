FROM alpine:3.6

ENV LANG=en_US.utf8 \
	DOCKER_USER=dockeru \
	DOCKER_USER_ID=1000 \
	DOCKER_USER_GROUP_ID=1000

COPY rootfs/ /

RUN apk --no-cache add curl bash && \
	apk --no-cache --repository http://dl-2.alpinelinux.org/alpine/edge/community add shadow && \
	mkdir /opt /docker-init.d && \
	addgroup -g $DOCKER_USER_GROUP_ID $DOCKER_USER && \
	adduser $DOCKER_USER -u $DOCKER_USER_ID -S -G $DOCKER_USER -s /bin/bash && \
	sed -i -e 's|/bin/sh|/bin/bash|' -e 's|/bin/ash|/bin/bash|' /etc/passwd && \
	curl -o /usr/local/bin/gosu -fsSL "https://github.com/tianon/gosu/releases/download/1.9/gosu-amd64" && \
	chmod +x /entrypoint.sh /usr/local/bin/gosu /usr/local/bin/*.sh

ENTRYPOINT ["/entrypoint.sh"]
