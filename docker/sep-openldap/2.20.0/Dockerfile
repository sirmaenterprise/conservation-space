FROM docker-reg.sirmaplatform.com/openldap:2.4.44

ENV SEP_MDB_SUFFIX="dc=SIRMAITT,dc=BG" \
	SEP_MDB_ROOT_DN="cn=Manager,dc=SIRMAITT,dc=BG" \
	SEP_MDB_DEFAULT_PASS="secret"

COPY rootfs/ /
