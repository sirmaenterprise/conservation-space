conservation-space
==================

For more info look at: http://www.conservationspace.org

The source code is distributed under GPLv3 license (http://www.gnu.org/copyleft/gpl.html).

# Building

## Required tools
1. Maven 3.x
2. golang 1.9
3. Docker
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

# Deploying

Deployment is achieve using a docker compose file. For it to work a docker swarm needs to be initialized and all required nodes to be joined in the swarm.
Note that images need to be in a docker registry (so that docker can pull them) or to be present on all machines.

There is also a proxy service (nginx) that is used for ssl termination and to proxy most services through a single address. It requires the `NGINX_SERVER_NAME` to be set to the public hostname of the machine running it. You can either `export` it beforehand or pass it in when deploying.

For ssl to work the certificate and public key must be placed inside `/etc/sep/keystores` and be named nginx.crt and nginx.key.

Also for graphdb to work you need to place your license file in `/etc/sep/licenses/graphdb.license`.

To deploy the compose file (stack) run:
```bash
NGINX_SERVER_NAME="example.com" docker stack deploy --compose-file ./docker-stack.yml sep
``` 
