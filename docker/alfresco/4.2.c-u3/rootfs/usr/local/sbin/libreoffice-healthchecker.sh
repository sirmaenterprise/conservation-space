#!/usr/bin/env bash

while true; do
	if ! wait-for-service.sh -h 127.0.0.1 -p 8100 -m 3 > /dev/null; then
		killall soffice.bin &> /dev/null

		/usr/lib/libreoffice/program/soffice.bin \
			--accept='socket,host=127.0.0.1,port=8100;urp;StarOffice.ServiceManager' \
			-env:UserInstallation=file:///var/lib/alfresco/oouser \
			--headless \
			--nocrashreport \
			--nofirststartwizard \
			--nologo \
			--norestore &
	fi

	sleep 1;
done
