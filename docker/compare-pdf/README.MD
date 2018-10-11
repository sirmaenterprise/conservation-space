# Compare PDF service
This repo contains a docker image that is used to start a container in which is deployed nodejs service that
provides web services for comparing pdf files using the [diffpdf](https://github.com/taurus-forever/diffpdf-console/)
library. This git repository contains the diffpdf code which was forked before the point where it got commercial.

### To build the container simply run:
```bash
docker build --no-cache --quiet --tag docker-reg.sirmaplatform.com/compare-pdf:<version> .
```

### To start a container use the docker run command:
```bash
docker run --rm -it -p 8125:8125 docker-reg.sirmaplatform.com/compare-pdf:<version>
```

### To start compare server without the docker container simply use:
```bash
$ npm install
$ npm start
```

**Important** on headless linux operating systems `xvfb` must be installed and compare server must be started using `xvfb-run` command.

#### Available configurations
Compare can be configured via simple set of configuration described in `config.json`
* `host` (default to `0.0.0.0`) - host to bind and listen to.
* `port` (default to `8123`) - port to bind and listen to.
* `timeout` (default to `120000`) - timeout in ms before compare process is canceled and HTTP error 503 is returned.
* 'cleanTempInternally' - the downloaded files that are used for the compares are stored in a temp folder this option
 can be used to delete files older than 30 minutes.
* 'tempFolder' - the location of the tmp folder. If you change this keep in mind you will have to create the new
folder. The default configuration is created in the container by default.

## Compare PDF
Provides GET, POST and DELETE endpoints at `/compare`.
* 'GET' expects url param called id, which contains a filename, which if exists will be streamed back to the client.
* 'POST' expects a JSON payload with 2 parameters: 'firstURL' and 'secondURL', which has to be a downloadable emf pdf file content.
* 'DELETE' expects url param called id, which contains a filename. The services deletes the corresponding file.

## Health check
Simply responds with status 200 at `/health`.
