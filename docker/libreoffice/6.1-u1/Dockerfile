FROM ubuntu:xenial

RUN \
	apt-get update && \
	apt-get install -y software-properties-common && \
	add-apt-repository -y ppa:libreoffice/ppa && \
	apt-get autoremove -y --purge software-properties-common && \
	apt-get update && \
	apt-get -y --no-install-recommends install \
		openjdk-8-jre \
		imagemagick \
		ghostscript \
		libreoffice=1:6.* \
		libreoffice-writer=1:6.* \
		libreoffice-java-common=1:6.* \
		libreoffice-core=1:6.* \
		libreoffice-common=1:6.* \
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
		fonts-tlwg-purisa \
		fonts-crosextra-carlito && \
	fc-cache -f -v && \
	rm -rf /tmp/* /var/lib/apt/lists/*

COPY rootfs/ /
