conservation-space
==================

For more info look at: http://www.conservationspace.org

The source code is distributed under GPLv3 license (http://www.gnu.org/copyleft/gpl.html).

# Building

## Required tools
1. Java 1.8 JDK and JavaFX
1. Maven 3.x
2. golang 1.12.x
3. Docker 18.09.5
4. nodejs 6.x
5. npm 3.x

Install `gulp` and `jspm` globally by running (this requires root or sudo permissions):
```bash
npm install -g gulp jspm
```

Because jspm downloads (a lot) from github.com it is very likely to hit github's rate limit. To avoid this you need to create an API token or log into github. To do so execute `jspm config registries.github` and follow the instructions.

To build all artefacts and docker images run:
```bash
./build.sh
```

# Deployment

Deployment is achieved using a docker compose file. For it to work a docker swarm needs to be initialized and all required nodes to be joined in the swarm.
Note that images need to be in a docker registry (so that docker can pull them) or to be present on all machines.

For ssl to work the certificate and public key must be placed inside `/etc/sep/keystores` and be named nginx.crt and nginx.key.

Also for graphdb to work you need to place your license file in `/etc/sep/licenses/graphdb.license`.

## Service placement
The compose file uses docker's placement constraints. These are labels added to a service to constrain where it could be deployed.
Before deploying you need to set the appropriate node labels to the nodes in your swarm.

To do that - on the master node execute `docker node update --label-add <label> <node id | hostname>`. Where `label` is the label name + value that needs to be set e.g. `com.sirma.sep.solr.audit=yes` and `node id` is the given by docker to the target node (you can list all nodes using `docker node ls` command).

## Environment variables
Some services require variables that specify external to the swarm host names and address - update the following services by adding the specified env vars before deploying the compose file.

1. proxy
  1.1 `NGINX_SERVER_NAME` - this is the host name of the machine service all http requests e.g. `example.com`
  1.2. `NGINX_SERVER_NAME_INTERNAL` - some http services are proxied through port 8080 which should not be exposed to the world, usually this is paired with an internal address e.g. `internal.example.com`
  1.3. `PROXY_INTERNAL_SERVICE_ADDR` - the full base url for acccessing internal services e.g. `http://internal.example.com:8080`
  1.4. `PROXY_EXTERNAL_SERVICE_ADDR` - the base url for accessing public services e.g. https://example.com
2. wildfly
  2.1. `IMAGE_SERVER_BASE_URL` - base address at where the iiif server serves images - usually this is the same as `PROXY_EXTERNAL_SERVICE_ADDR`
3. iiif
  3.1. `BASE_URL` - bese url for iiif images - should be in the form <IMAGE_SERVER_BASE_URL>/iiif/fcgi-bin/iipsrv.fcgi?IIIF=

## Service dependencies
Some services require other to be up and running before they can start properly e.g. `wildfly` depends on the `db` service. This is controlled by an environment variable called `SERVICE_DEPENDENCIES`. It's value is a space separated list of `service_name:port` e.g. `SERVICE_DEPENDENCIES=db:5432 keycloak:8080`.

Also each service that has dependencies mounts the [0-wait-dependencies.sh](./0-wait-dependencies.sh) script, which actually parses the variable and checks it the service is up.
This script must be placed under `/etc/sep/stacks/bin` directory on all nodes deploying services with dependencies.

## Deploying the stack
To deploy the compose file (stack) run:
```bash
docker stack deploy --compose-file ./docker-stack.yml sep
```
