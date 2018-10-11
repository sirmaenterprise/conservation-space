#!/bin/bash

set -eu

cd $1

echo "Packaging modules into bundles"
gulp dist

BASE_IMAGE=$(head -n 1 Dockerfile | cut -d \  -f 2)
docker pull $BASE_IMAGE

echo "Build docker image docker-reg.sirmaplatform.com/seip-ui:$2"
docker build --tag=docker-reg.sirmaplatform.com/seip-ui:$2 .