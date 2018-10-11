# SpringBoot service for content preview and thumbnail generation

## Communication
Communication with the service is accomplished via JMS queues:
* ContentPreviewQueue - queue listener accepting content for preview and thumbnail generation
* ContentThumbnailQueue - queue listener accepting content only for thumbnail generation
* ContentPreviewCompletedQueue - queue where the generated preview is sent back
* ContentThumbnailCompletedQueue - queue where the generated thumbnail is sent back

## Generation
Internally it uses LibreOffice for the preview generation and ImageMagick for thumbnails.
If the provided content is of application/pdf mimetype then only a thumbnail will be generated.

## Docker packaging
The service is packaged within a Docker image which can be run like so:

`docker run -d -e JMS_HOST=<jms-host> docker-reg.sirmaplatform.com/content-preview:<version>`

Example environment variables are:
* `JMS_HOST` - the host where the JMS broker can be accessed
* `JMS_PORT` - the broker port
* `JMS_USER` - the username for authentication in the broker
* `JMS_PASS` - the password for authentication in the broker
See [Dockerfile] for the full list.

For full control over the started service use:

`docker run -d docker-reg.sirmaplatform.com/content-preview:<version> java -jar content-preview.jar <arguments>`