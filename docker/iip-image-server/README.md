Directories:
* output directory - where the processed (output) files are stored

Environment variables:
* `BASE_URL` - base url of the IIIF server. If the server is behind a proxy or mapped to a port other that `80` use this variable to override.

Sample run:
```
docker run -d \
	-v <output directory>:/var/www/localhost/images \
	-p <port>:80 \
	-e "BASE_URL=<external iiif base url>"
	--restart=on-failure \
	docker-reg.sirmaplatform.com/iip-server:6.0.0
```

Run as a service (Trigger restart from unhealthy status):
Docker service requires swarm mode on

```
docker service create --name iip-server \
	--publish 8999:80 \
	--mount type=bind,source=<absolute_path>/output,destination=/var/www/localhost/images \
	--env BASE_URL=<external iiif base url> \
	docker-reg.sirmaplatform.com/iip-server:6.0.0
```
