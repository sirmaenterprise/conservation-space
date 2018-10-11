#!/bin/sh

curl -i \
	-H "Content-Type: application/vnd.seip.v2+json" \
	-H "Cookie: dummy" \
	-H "Authorization: none" \
	-X POST  \
	-d '{"firstURL":"http://127.0.0.1:8125/static/healthcheck.pdf","secondURL":"http://127.0.0.1:8125/static/healthcheck.pdf"}' \
	--fail -s 127.0.0.1:8125/compare

if [ ! $? = 0 ]; then
    echo "The compare app is not responsive!"
    exit 1
fi

xdpyinfo -display :99.0 > /dev/null 2>&1
if [ ! $? = 0 ]; then
    echo "The X server is not running!"
    exit 1
fi
