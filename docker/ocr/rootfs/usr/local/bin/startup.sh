#!/bin/sh
set -eu

export TESSDATA_PREFIX=/usr/share/

java \
    -Dtesseract.datapath=${OCR_DATA_PATH} \
    -Dspring.hornetq.host=${JMS_HOST} \
    -Dtesseract.mode=${OCR_MODE} \
    -Dtesseract.language=${OCR_LANGUAGE} \
    -jar ${JAR_NAME}