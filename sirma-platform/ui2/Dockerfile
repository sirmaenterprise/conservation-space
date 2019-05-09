FROM docker-reg.sirmaplatform.com/nginx:1.12.2-u2

COPY docker-rootfs/ /
COPY build/ /sep/
COPY jspm_packages/ /sep/jspm_packages/

RUN sed 's|application/font-woff                 woff;|application/font-woff                 woff;\n    application/font-woff2                woff2;|g' -i /etc/nginx/mime.types
