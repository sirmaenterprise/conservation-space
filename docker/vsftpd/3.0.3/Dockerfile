ARG docker_registry=
FROM $docker_registry/base:3

ENV \
	FTP_USER="ftp" \
	FTP_USER_PASS="ftp" \
	PASV_MIN_PORT="4559" \
	PASV_MAX_PORT="4564" \
	PASV_PROMISCUOUS="NO" \
	RUN_AS_ROOT="yes" \
	VOLUME_VSFTPD_DATA=/var/lib/vsftpd

COPY rootfs/ /

RUN \
	apk --no-cache add build-base curl linux-pam-dev vsftpd apache2-utils && \
	chmod +x /usr/local/bin/*.sh && \
	curl -sSL https://github.com/tiwe-de/libpam-pwdfile/archive/v1.0.tar.gz | tar xz && \
	(cd libpam-pwdfile-1.0/; make install) && \
	mkdir -p /var/lib/vsftpd && \
	chown -R $DOCKER_USER:$DOCKER_USER /var/lib/vsftpd && \
	rm -rf libpam-pwdfile-1.0/ && \
	apk --no-cache del build-base linux-pam-dev

VOLUME /var/lib/vsftpd

EXPOSE 21

HEALTHCHECK --interval=15s --timeout=3s CMD healthcheck.sh

CMD ["vsftpd", "/etc/vsftpd/vsftpd.conf"]
