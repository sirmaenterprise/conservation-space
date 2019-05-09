#!/usr/bin/env bash

set -euo pipefail

script_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
log_file="$script_dir/build.log"
current_dir="$(pwd)"

mvn_local_repo="${MAVEN_REPO:-$HOME/.m2/repository}"

function cleanup() {
        cd "$current_dir"
        echo 'cleaning...'
        sync
}

trap cleanup EXIT

exec &> >(tee "$log_file")

echo "Building UI..."
cd $script_dir/sirma-platform/ui2
npm --progress=false install
jspm install
gulp compile
jspm install --dev
gulp dist
cd $current_dir

for dependency in excel-streaming-reader zimbra-soap-client; do
        echo "Building ${dependency}..."
        mvn -Dmaven.repo.local=$mvn_local_repo -q -f "$script_dir/sirma-platform/$dependency/pom.xml" clean install
done

echo "Building EAI..."
mvn \
        -Dmaven.repo.local=$mvn_local_repo \
        -Dkeystore.password="123456" \
        -Dkeystore.alias=myorg \
        -Dkeystore.path=$script_dir/sirma-platform/eai/self-signed.jks \
        -DskipTests=true \
        -q -f "$script_dir/sirma-platform/eai/pom.xml" clean install


echo "Building Sirma Enterprise Platform..."
mvn -Dmaven.repo.local=$mvn_local_repo -q -f $script_dir/sirma-platform/pom.xml -N install
mvn -Dmaven.repo.local=$mvn_local_repo -q -f $script_dir/sirma-platform/bom/pom.xml install
mvn \
        -Duser.timezone=UTC \
        -DtrimStackTrace=false \
        -DskipTests=true \
        -Dmaven.repo.local=$mvn_local_repo -q -f "$script_dir/sirma-platform/pom.xml" -T 1C clean install

echo "Building Conservation Space deployment..."
mvn -Dmaven.repo.local=$mvn_local_repo -q -f "$script_dir/cs-deployment/pom.xml" clean package

echo "Building Alfresco..."
mvn -Dmaven.repo.local=$mvn_local_repo \
        -q \
        -f "$script_dir/sirma-platform/alfresco/alfresco-emf-integration/pom.xml" \
        -T 1C clean package

mvn -Dmaven.repo.local=$mvn_local_repo \
        -q \
        -f "$script_dir/sirma-platform/alfresco/alfresco-emf-integration/pom.xml" \
        -T 1C install

echo "Building OCR..."
mvn -Dmaven.repo.local=$mvn_local_repo \
        -q \
        -f "$script_dir/docker/ocr/pom.xml" \
        clean install

echo "Building Content Preview..."
mvn -Dmaven.repo.local=$mvn_local_repo \
        -q \
        -f "$script_dir/docker/content-preview/pom.xml" \
        clean install

function build_docker_image() {
        local tag="$1"
        local ctx="$2"

        # --quiet \
        docker image build \
                --rm --no-cache \
                --tag $tag \
                $ctx
}

version="$(mvn -q -N -f cs-deployment/pom.xml -Dexec.executable=echo -Dexec.args='${project.version}' exec:exec)"
echo "sep/cs version: $version"
build_docker_image "docker-reg.sirmaplatform.com/base:1" "$script_dir/docker/base/1"
build_docker_image "docker-reg.sirmaplatform.com/base:2" "$script_dir/docker/base/2"
build_docker_image "docker-reg.sirmaplatform.com/base:2.0.1" "$script_dir/docker/base/2.0.1"
build_docker_image "docker-reg.sirmaplatform.com/base:3.0.0" "$script_dir/docker/base/3.0.0"

build_docker_image "docker-reg.sirmaplatform.com/nginx:1.12.2-u2" "$script_dir/docker/nginx/1.12.2-u2"
build_docker_image "docker-reg.sirmaplatform.com/nginx:1.12.2-u5" "$script_dir/docker/nginx/1.12.2-u5"

build_docker_image "docker-reg.sirmaplatform.com/postgres:9" "$script_dir/docker/postgres/9"
build_docker_image "docker-reg.sirmaplatform.com/sep-postgres:2.24.0" "$script_dir/docker/sep-postgres/2.24.0"

build_docker_image "docker-reg.sirmaplatform.com/openjdk-jre:8" "$script_dir/docker/openjdk-jre/8"
build_docker_image "docker-reg.sirmaplatform.com/openjdk-jre:8-1" "$script_dir/docker/openjdk-jre/8-1"

build_docker_image "docker-reg.sirmaplatform.com/solr:5.5.3-u1" "$script_dir/docker/solr/5.5.3-u1"
build_docker_image "docker-reg.sirmaplatform.com/sep-solr-audit:2.12.0-u1" "$script_dir/docker/sep-solr-audit/2.12.0-u1"

build_docker_image "docker-reg.sirmaplatform.com/solr:6.5.0-u1" "$script_dir/docker/solr/6.5.0-u1"
build_docker_image "docker-reg.sirmaplatform.com/sep-solr-core:2.12.0-u1" "$script_dir/docker/sep-solr-core/2.12.0-u1"

build_docker_image "docker-reg.sirmaplatform.com/export:$version" "$script_dir/docker/export"
build_docker_image "docker-reg.sirmaplatform.com/compare-pdf:$version" "$script_dir/docker/compare-pdf"

build_docker_image "docker-reg.sirmaplatform.com/openjdk-jre:7-base-1" "$script_dir/docker/openjdk-jre/7-base-1"
build_docker_image "docker-reg.sirmaplatform.com/openjdk-jdk:7" "$script_dir/docker/openjdk-jdk/7"
build_docker_image "docker-reg.sirmaplatform.com/openjdk-jdk:8-u1" "$script_dir/docker/openjdk-jdk/8-u1"
build_docker_image "docker-reg.sirmaplatform.com/wso2idp:5" "$script_dir/docker/wso2idp/5"
build_docker_image "docker-reg.sirmaplatform.com/sep-wso2is:$version" "$script_dir/docker/sep-wso2is"
docker image build \
        --rm --no-cache \
        --build-arg base_image=docker-reg.sirmaplatform.com/sep-wso2is:$version \
        --tag docker-reg.sirmaplatform.com/cs-wso2is:$version \
        $script_dir/docker/cs-wso2is

build_docker_image "docker-reg.sirmaplatform.com/keycloak:4.3.0-u1" "$script_dir/docker/keycloak/4.3.0-u1"
build_docker_image "docker-reg.sirmaplatform.com/sep-keycloak:$version" "$script_dir/sirma-platform/keycloak-integration"
docker image build \
        --rm --no-cache \
        --build-arg base_image=docker-reg.sirmaplatform.com/sep-keycloak:$version \
        --tag docker-reg.sirmaplatform.com/cs-keycloak:$version \
        $script_dir/docker/cs-keycloak

build_docker_image "docker-reg.sirmaplatform.com/openldap:2.4.44" "$script_dir/docker/openldap/2.4.44"
build_docker_image "docker-reg.sirmaplatform.com/sep-openldap:2.25.0" "$script_dir/docker/sep-openldap/2.25.0"

build_docker_image "docker-reg.sirmaplatform.com/graphdb:8.3.1-u5" "$script_dir/docker/graphdb/8.3.1-u5"

build_docker_image "docker-reg.sirmaplatform.com/ocr-service:$version" "$script_dir/docker/ocr"

build_docker_image "docker-reg.sirmaplatform.com/libreoffice:6.1-u1" "$script_dir/docker/libreoffice/6.1-u1"
build_docker_image "docker-reg.sirmaplatform.com/content-preview:$version" "$script_dir/docker/content-preview"

build_docker_image "docker-reg.sirmaplatform.com/image-processing:1.0.2" "$script_dir/docker/image-processing"

build_docker_image "docker-reg.sirmaplatform.com/iip-server:6.0.0" "$script_dir/docker/iip-image-server"

build_docker_image "docker-reg.sirmaplatform.com/vsftpd:3.0.3" "$script_dir/docker/vsftpd/3.0.3"

build_docker_image "docker-reg.sirmaplatform.com/apache-server:1.1" "$script_dir/docker/apache-server/1.1"

(
        mkdir -p "$GOPATH/src/git.sirmaplatform.com/stash/seip"
        ln -s "$script_dir/docker/http-proxy" "$GOPATH/src/git.sirmaplatform.com/stash/seip/http-proxy"
        cd "$GOPATH/src/git.sirmaplatform.com/stash/seip/http-proxy"
        dep ensure
        mkdir -p "rootfs/usr/local/bin"
        go build -o rootfs/usr/local/bin/http-proxy-linux-amd64 cli/main.go
)

build_docker_image "docker-reg.sirmaplatform.com/http-proxy:$version" "$script_dir/docker/http-proxy"

build_docker_image "docker-reg.sirmaplatform.com/wildfly:9.0.2-u5" "$script_dir/docker/wildfly/9.0.2-u5"

echo "Building base wildfly docker image..."
docker image build \
        --rm --no-cache --quiet \
        --tag docker-reg.sirmaplatform.com/sep-base-wildfly:$version \
        --file $script_dir/sirma-platform/platform/Dockerfile.base \
        $script_dir/sirma-platform/platform

echo "Building Coservation Space wildfly docker image..."
docker image build \
        --rm --no-cache --quiet \
        --build-arg sep_base_image_version=$version \
        --tag docker-reg.sirmaplatform.com/cs-wildfly:$version \
        $script_dir/cs-deployment

echo "Building Alfresco docker image..."
build_docker_image "docker-reg.sirmaplatform.com/alfresco:4.2.c-u3" "$script_dir/docker/alfresco/4.2.c-u3"
docker image build \
        --rm --no-cache \
        --tag docker-reg.sirmaplatform.com/sep-alfresco:$version \
        $script_dir/sirma-platform/alfresco

echo "Building UI docker image..."
docker image build \
        --rm --no-cache --quiet \
        --tag docker-reg.sirmaplatform.com/sep-ui:$version \
        $script_dir/sirma-platform/ui2
