#!/bin/sh

set -e

replace-env.sh /etc/nginx/nginx.conf

for config in /etc/nginx/conf.d/*.conf; do
	replace-env.sh "$config"
done

mkdir -p /run/nginx
