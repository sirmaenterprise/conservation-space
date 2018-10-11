#!/bin/bash
set -eu

echo "Publish docker image docker-reg.sirmaplatform.com/seip-ui:$2"
docker push docker-reg.sirmaplatform.com/seip-ui:$2
docker rmi docker-reg.sirmaplatform.com/seip-ui:$2