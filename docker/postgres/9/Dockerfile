FROM docker-reg.sirmaplatform.com/base:2

ENV VOLUME_PGDATA="/var/lib/postgresql/data"\
	WORK_MEM="160MB"\
	MAX_CONNECTIONS="300"\
	MAX_PREPARED_TRANSACTIONS="100"\
	EFFECTIVE_CACHE_SIZE="512MB"\
	SHARED_BUFFERS="256MB"\
	LISTEN_ADDRESSES="*"\
	POSTGRES_PASS="postgresql"

COPY rootfs/ /

RUN \
	echo 'http://dl-cdn.alpinelinux.org/alpine/v3.4/main' > /etc/apk/repositories && \
	echo 'http://dl-cdn.alpinelinux.org/alpine/v3.4/community' >> /etc/apk/repositories && \
	mkdir -p $VOLUME_PGDATA			&& \
	chown $DOCKER_USER:$DOCKER_USER $VOLUME_PGDATA && \
	chmod +x /docker-init.d/*.sh 	&& \
	chmod +x /usr/sbin/healthcheck.sh && \
	apk --no-cache add postgresql

VOLUME /var/lib/postgresql/data

HEALTHCHECK --interval=10s --timeout=3s CMD healthcheck.sh

EXPOSE 5432
CMD ["postgres"]
