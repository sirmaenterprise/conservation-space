FROM alpine:3.8

ENV \
	JAR_NAME=ocr-service.jar \
	JAVA_HOME="/usr/lib/jvm/java-1.8-openjdk/jre" \
	OCR_DATA_PATH="/usr/share" \
	OCR_MODE="0" \
	JMS_HOST="wildfly" \
	OCR_LANGUAGE="eng"

WORKDIR /opt/ocr

COPY target/${JAR_NAME} .
COPY rootfs/ /

RUN \
	apk add --no-cache \
		curl \
		fontconfig \
		ttf-dejavu \
		openjdk8-jre-base \
		ghostscript \
		tesseract-ocr \
		tesseract-ocr-data-bul \
		tesseract-ocr-data-dan \
		tesseract-ocr-data-spa \
		tesseract-ocr-data-chi_sim tesseract-ocr-data-chi_tra \
		tesseract-ocr-data-hin \
		tesseract-ocr-data-ara \
		tesseract-ocr-data-por \
		tesseract-ocr-data-ben \
		tesseract-ocr-data-rus \
		tesseract-ocr-data-fra \
		tesseract-ocr-data-sqi \
		tesseract-ocr-data-deu \
		tesseract-ocr-data-fin \
		tesseract-ocr-data-jpn && \
	chmod +x /usr/local/bin/*

HEALTHCHECK --interval=10s --timeout=3s --retries=3 --start-period=15s CMD healthcheck.sh

ENTRYPOINT ["startup.sh"]
