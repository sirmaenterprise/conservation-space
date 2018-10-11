#!/usr/bin/env bash

set -e

replace-env.sh /etc/rsyslog.d/openldap.conf

# Manually start it due to lack of init system in Alpine base
rsyslogd
