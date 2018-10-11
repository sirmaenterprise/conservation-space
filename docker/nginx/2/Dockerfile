FROM docker-reg.sirmaplatform.com/base:2

RUN apk add --no-cache nginx

COPY rootfs/ /

# By declaring an explicit volume you bypass docker's copy-on-write backend (e.g. aufs)
# and bind-mount the data directly from the host filesystem.
VOLUME ["/var/cache/nginx"]

ENV RUN_AS_ROOT="yes" \
	SERVICE_DIR_VAR_LIB_NGINX="/var/lib/nginx/"

# http and ssl ports
EXPOSE 80 443

HEALTHCHECK --interval=10s --timeout=3s CMD curl --fail -s 127.0.0.1:8888/status || exit 1

CMD ["nginx", "-g", "daemon off;"]
