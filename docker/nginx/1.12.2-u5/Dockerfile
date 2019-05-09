FROM docker-reg.sirmaplatform.com/base:2.0.1

ENV \
	RUN_AS_ROOT=yes \
	NGINX_MAX_REQUEST_SIZE=10M \
	NGINX_CLIENT_BODY_BUFF_SIZE=1M \
	NGINX_MULTI_ACCEPT=on \
	NGINX_WORKER_CONNECTIONS=1024 \
	NGINX_WORKER_PROCESSES=4 \
	NGINX_RLIMIT_NOFILE=10000 \
	NGINX_LOG_LEVEL=error \
	SERVICE_NAME=nginx

RUN apk add --no-cache nginx=1.12.2-r2 && \
	mkdir -p /var/tmp/nginx /var/cache/nginx /var/log/nginx && \
	chown -R $DOCKER_USER:$DOCKER_USER /var/tmp/nginx /var/cache/nginx /var/log/nginx

COPY rootfs/ /

VOLUME ["/var/log/nginx"]

EXPOSE 80 443

HEALTHCHECK --timeout=3s CMD healthcheck.sh

CMD ["nginx", "-g", "daemon off;"]
