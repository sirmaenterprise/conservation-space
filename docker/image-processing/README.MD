Directories:
* /data - the input and processed dirs are created under this one, it is where the files for processing and pocessed are stored
* output directory - where the processed (output) files are stored

Fallback image:
* Image with name 'failed.ptif' should be uploaded. It will be used as a fallback when conversion of a particular image fails.

ICC profiles:
* To add additional color profile to the image use, mount it to the following file - /opt/profile.icc

PP3 profile for RawTherapee:
* pp3 profile has to be mounted to /opt/default.pp3 it is required for rawtherapee transofmrations

Restart policy:
The container is started with the python script for image processing in the foreground.
The script may break in some situations (probably a bug). Setting docker restart policy to 'on-failure' will cause Docker to restart the container on fail.

Sample run:
```
docker run -d \
	-v <data directory>:/data \
	-v <output directory>:/var/www/localhost/images \
	-v <path to the pp3 profile>:/opt/default.pp3 \
	-v <path to the icc profile>:/opt/profile.icc \
	--restart=on-failure \
	docker-reg.sirmaplatform.com/image-processing:1.0.2
```

Run as a service (Trigger restart from unhealthy status):
Docker service requires swarm mode on

```
docker service create --name iip-server \
	--mount type=bind,source=<absolute_path>,destination=/data \
	--mount type=bind,source=<absolute_path>/output,destination=/var/www/localhost/images \
	docker-reg.sirmaplatform.com/image-processing:1.0.2
```
