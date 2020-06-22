FROM debian:stretch

ARG DEBIAN_FRONTEND=noninteractive

ENV VOLUME_DATA="/var/www/html"

RUN \
	apt-get update && \
	apt-get install -y apache2 && \
	chown -R $DOCKER_USER:$DOCKER_USER $VOLUME_DATA

EXPOSE 80

CMD service apache2 start && while true; do sleep 1; done
