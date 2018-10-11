FROM docker-reg.sirmaplatform.com/nginx:2

ENV BACKEND_ADDR="backend" \
  BACKEND_PROTOCOL="https" \
  BACKEND_CONTEXT_PATH="" \
  NGINX_MAX_REQUEST_SIZE="2000M" \
  NGINX_PROXY_TIMEOUT="3600" \
  GZIP_ENABLED="on"

ADD docker/main.conf /etc/nginx/conf.d/
ADD build /seip
ADD jspm_packages /seip/jspm_packages

RUN rm -f /etc/nginx/conf.d/default.conf && \
    sed 's|application/font-woff                 woff;|application/font-woff                 woff;\n    application/font-woff2                woff2;|g' -i /etc/nginx/mime.types

