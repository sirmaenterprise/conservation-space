FROM debian:stretch

ARG DEBIAN_FRONTEND=noninteractive

RUN \
	apt-get update && \
	apt-get install -y apache2

EXPOSE 80

CMD service apache2 start && while true; do sleep 1; done
