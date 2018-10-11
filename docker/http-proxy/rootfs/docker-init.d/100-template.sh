#!/usr/bin/env bash

set -eu

proxytpl /etc/sep/proxy/sep.conf.tpl /etc/sep/proxy/vars/ > /etc/nginx/conf.d/default.conf
sync

for config in /etc/nginx/conf.d/*.conf; do
	replace-env.sh "$config"
done

sync
cat /etc/nginx/conf.d/default.conf