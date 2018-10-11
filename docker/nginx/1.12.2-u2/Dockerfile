FROM docker-reg.sirmaplatform.com/base:2.0.1

ENV RUN_AS_ROOT=yes \
	NGINX_MAX_REQUEST_SIZE=10M \
	NGINX_CLIENT_BODY_BUFF_SIZE=1M \
	NGINX_MULTI_ACCEPT=on \
	NGINX_WORKER_CONNECTIONS=1024 \
	NGINX_WORKER_PROCESSES=4 \
	NGINX_RLIMIT_NOFILE=10000

RUN apk add --no-cache nginx=1.12.2-r1 && \
	mkdir -p /var/tmp/nginx /var/cache/nginx && \
	chown -R $DOCKER_USER:$DOCKER_USER /var/tmp/nginx /var/cache/nginx

COPY rootfs/ /

VOLUME ["/var/cache/nginx", "/var/tmp/nginx"]

EXPOSE 80 443

HEALTHCHECK --interval=10s --timeout=3s CMD curl --fail -s 127.0.0.1:8888/status || exit 1

CMD ["nginx", "-g", "daemon off;"]
