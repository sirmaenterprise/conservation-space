FROM ubuntu:17.10

COPY rootfs/ /

RUN \
	chmod +x /entrypoint.sh && \
	# Update packages and install tools
	apt-get update -y && apt-get install -y \
	autoconf \
	apache2 \
	build-essential \
	git \
	libapache2-mod-fcgid \
	libtool \
    libjpeg-dev \
    libtiff5-dev \
	memcached \
	make \
	wget \
	gettext-base \
	pkg-config && \
	cd /opt && \
	# Install and setup image server
    mkdir /opt/iipsrv && \
    cd /opt/iipsrv && \
    git clone https://github.com/sirmaenterprise/iipsrv ./ && \
    git checkout tags/v2.9 && \
    chmod +x autogen.sh && sleep 2 && ./autogen.sh && \
    chmod +x configure && sleep 2 && ./configure && sleep 2 && make && make install && \
    # copy iipsrv binary to fcgi-bin
    mkdir -p /var/www/localhost/fcgi-bin && \
    cp src/iipsrv.fcgi /var/www/localhost/fcgi-bin && \
    cd ../ && \
    rm -rf iipsrv && \
    # create image dir, images are placed inside mapped host directory (see README)
    mkdir -p /var/www/localhost/images/ && \
    chown -R www-data:www-data /var/www/ && \
    # enable headers
    a2enmod headers && \
    # enable fcgid mod
    a2enmod fcgid && \
    # disable default.conf
    a2dissite 000-default.conf && \
    # enable our site conf
    a2ensite 001-iipsrv.conf && \
    # enable cors
    a2enconf cors.conf && \
    # Remove intermediate packages and clean-up apt-get cache \
    apt-get autoremove -y --purge software-properties-common build-essential git make pkg-config autoconf && \
    apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*



EXPOSE 80

ENTRYPOINT ["/entrypoint.sh"]

HEALTHCHECK --interval=1m --timeout=10s --retries=3 \
			CMD wget -O /dev/null -q http://localhost:80 || exit 1

VOLUME ["/var/www/localhost/images"]
