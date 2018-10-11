FROM ubuntu:xenial

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && \
	apt-get -y --no-install-recommends install \
	openjdk-8-jre \
	imagemagick \
	ghostscript \
	libreoffice \
	libreoffice-writer \
	libreoffice-java-common \
	libreoffice-core \
	libreoffice-common \
	ure \
	hyphen-fr \
	hyphen-de \
	hyphen-en-us \
	hyphen-it \
	hyphen-ru \
	fonts-dejavu \
	fonts-dejavu-core \
	fonts-dejavu-extra \
	fonts-dustin \
	fonts-f500 \
	fonts-fanwood \
	fonts-freefont-ttf \
	fonts-liberation \
	fonts-lmodern \
	fonts-lyx \
	fonts-noto \
	fonts-opensymbol \
	fonts-sil-gentium \
	fonts-texgyre \
	fonts-tlwg-purisa && \
	rm -rf /tmp/* /var/lib/apt/lists/*

COPY rootfs/ /
