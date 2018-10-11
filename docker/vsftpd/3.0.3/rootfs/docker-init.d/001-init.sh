#!/usr/bin/env sh

set -e

replace-env.sh /etc/vsftpd/vsftpd.conf

if [ "x$PASV_ADDRESS" != "x" ]; then
	echo "pasv_address=$PASV_ADDRESS" >> /etc/vsftpd/vsftpd.conf
fi

mkdir -p /var/lib/vsftpd
chown -R $DOCKER_USER:$DOCKER_USER /var/lib/vsftpd

htpasswd -bcd /etc/vsftpd/vsftpd.passwd $FTP_USER $FTP_USER_PASS
