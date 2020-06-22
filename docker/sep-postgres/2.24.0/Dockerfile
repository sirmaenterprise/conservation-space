ARG docker_registry=
FROM $docker_registry/postgres:9

ENV ROLE_PASS_ADMIN="admin" \
	ROLE_PASS_SEP="sep"

COPY docker-init.d/ /docker-init.d

RUN chmod +x /docker-init.d/*.sh
