#!/usr/bin/env bash

set -eu

ldapwhoami -H ldap://127.0.0.1:10389 -x
