#!/bin/sh

set -eu

curl -u ${FTP_USER}:${FTP_USER_PASS} --fail -s ftp://0.0.0.0:21 || exit 1
