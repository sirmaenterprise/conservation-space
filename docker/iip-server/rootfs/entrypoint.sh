#!/usr/bin/env bash

for f in /etc/apache2/sites-available/*.conf; do
	echo "replacing vars in $f"
	envsubst < "$f" > "$f.tmp"
	mv -f "$f.tmp" "$f"
done

sync

source /etc/apache2/envvars
/usr/sbin/apache2 -k start -D FOREGROUND
