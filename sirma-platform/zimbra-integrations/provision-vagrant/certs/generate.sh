# This file will generate default TEST self-signed certificates to be used by Zimbra,
#!/bin/sh

set -eu

rm domain.crt domain.csr domain.key

sync
# Generate CRT,KEY and CSR files.
# Root certificate
openssl req -newkey rsa:2048 -sha256 -nodes -keyout domain.key \
       	-x509 -days 365 -out domain.crt -extensions v3_req \
       	-config ./certs.config && openssl req -new -sha256 \
	-key domain.key -out domain.csr -config certs.config