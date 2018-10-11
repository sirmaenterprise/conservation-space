## About

This repo contains a spring boot application that allows the OCR of documents from the Sirma Enterprise Platform. When 
the application is run it connects to a specific JMS Queue, which is exposed in the platform. Each document that is 
put in the queue is consume and OCRed. After the processing is completed the OCRed content is written back to another 
queue at SES. 

The repo also contains:
* a Dockerfile, used to create a container for the OCR Service.
* a Jenkinsfile, used to build the application, build the container and push it to the docker registry.
 
## Optical Character Recognition(OCR)
The Optical Character Recognition (OCR) service is implemented using spring-boot and Tesseract. The OCR tool is 
integrated via Java Native Access (JNA, https://en.wikipedia.org/wiki/Java_Native_Access). 

## Sirma Enterprise Platform Integration
The integration with SEP is done via JMS queues. There are two queues exposed at SEP one for pending for OCR 
documents and one for already OCRed document. The OCR service reads from the first, executes the OCR and writes to 
the second queue.
 
## Configurations
The necessary configuration for the spring boot service can be found in \src\main\resources\application.properties. 
The configurations in this file can be overridden when starting the jar. Can be done similar to:

`java -Dtesseract.datapath=value -jar ocr-service.jar` 

This will override the configuration `tessaract.datapath` from the configuration file.

## Installing the Service 
Every time code is pushed to the repo a new docker image is built and deployed to the docker repo. In order to 
install it one must:
* Pull the image if you don't have it in the local repo with `docker pull  docker-reg.sirmaplatform.com/ocr-service:<insert-version-of-the-image>`
* Start the container: `docker run --rm -it -d -e "OCR_DATA_PATH=/usr/share" -e "OCR_MODE=0" -e "JMS_HOST=10.0.2.2" -e "OCR_LANGUAGE=eng" docker-reg.sirmaplatform.com/ocr-service:<insert-version-of-the-image>`

When starting the container we have to provide 3 arguments as environment variables (the -e arguments). There are 
default values which you can see in Dockerfile.
* `-e "OCR_DATA_PATH=/usr/share"` this should be the path to the tessdata folder. For this container it should always be 
/usr/share.
* `-e "OCR_MODE=0"` is the OCR mode, 0 will be used for most cases, for more information, refer to Tesseract/Tess4j 
documentation.
* `-e "JMS_HOST=10.0.2.2"` is the host of the corresponding SES application.
* `-e "OCR_LANGUAGE=eng"` is the language from which we do OCR. For instance eng stands for english. The language is 
provided from the platform. This configuration is only used when the one from the jms message is missing.

If one of these arguments is not supplied then the start script will print error and will fail to bootstrap the 
application. Also those environment variables have provided default values which can be `seen in Dockerfile`.