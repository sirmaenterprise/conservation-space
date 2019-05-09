# Keycloak identity provider

Base image for JBoss's Keycloak IDP. Although it can be used like this, this image's purpose is to be extended with
custom configurations and database connections.

Example use: `docker run -d -p 8080:8080 --name="keycloak" docker-reg.sirmaplatform.com/keycloak:2`