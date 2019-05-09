#!/usr/bin/env bash

set -eu

/usr/local/bin/http-proxy-linux-amd64 -config /etc/sep/proxy/http-proxy.yaml &
