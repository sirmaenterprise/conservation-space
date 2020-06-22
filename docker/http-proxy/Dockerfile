ARG docker_registry=
FROM $docker_registry/nginx:1.16.1

ENV \
	NGINX_SERVER_NAME="_" \
	NGINX_SSL_PROTOCOLS="TLSv1.2 TLSv1.3" \
	NGINX_SSL_CIPHERS="ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384:ECDHE-ECDSA-CHACHA20-POLY1305:ECDHE-RSA-CHACHA20-POLY1305:DHE-RSA-AES128-GCM-SHA256:DHE-RSA-AES256-GCM-SHA384" \
	NGINX_MAX_REQUEST_SIZE="2000M" \
	NGINX_PROXY_TIMEOUT="3600" \
	GZIP_ENABLED="on" \
	PROXY_LOG_LEVEL="DEBUG"

COPY rootfs/ /

RUN \
	sed 's|application/font-woff                 woff;|application/font-woff                 woff;\n    application/font-woff2                woff2;|g' -i /etc/nginx/mime.types && \
	chmod +x /usr/local/bin/http-proxy-linux-amd64

EXPOSE 8080
