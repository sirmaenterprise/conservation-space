FROM alpine:3.4

ENV LANG en_US.utf8
ENV DOCKER_USER=dockeru
ENV DOCKER_REMOTE_PORT=4243
ENV DOCKER_REMOTE_PROTO=http

COPY ["entrypoint.sh", "/"]
COPY ["scripts/", "/usr/sbin/"]

RUN apk --no-cache add curl && \
	mkdir /opt /docker-init.d && \
\
	addgroup $DOCKER_USER && \
	adduser $DOCKER_USER -S -G $DOCKER_USER -s /bin/sh && \
\
	curl -sL "https://github.com/stedolan/jq/releases/download/jq-1.5/jq-linux64" > /usr/local/bin/jq && \
	curl -o /usr/local/bin/gosu -fsSL "https://github.com/tianon/gosu/releases/download/1.9/gosu-amd64" && \
	chmod +x /entrypoint.sh /usr/local/bin/gosu /usr/sbin/*.sh /usr/local/bin/jq

ENTRYPOINT ["/entrypoint.sh"]
