#!/usr/bin/env bash

set -eu -o pipefail

if [ -n "${DEBUG:-}" ]; then
	set -x
fi

script_dir="$(cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd)"
temp_dir="$(mktemp -d)"
log_file="$temp_dir/build-$(date '+%Y-%m-%d_%H.%M.%S').log"

default_version="2.28.0-SNAPSHOT"
docker_reg="$1"

function is_image_built() {
	local search="$1"
	if docker image ls --format '{{.Repository}}:{{.Tag}}' | grep "$search" > /dev/null; then
		return 0
	fi
	return 1
}

function build_dependency() {
	local dockerfile="$1"
	local dependency=""
	! while IFS= read -r line; do
		if [[ $line == FROM* ]]; then
			dependency="$(echo $line | cut -d' ' -f2)"
			break
		fi
	done < $dockerfile


	if [ -z "$dependency" ]; then
		echo "Unable to find parent image of $dockerfile"
		exit 1
	fi

	if [[ $dependency == \$docker_reg* ]]; then
		build_image_versions "$(echo $dependency | cut -d'/' -f2 | cut -d':' -f1)"
	fi
}

function build_image() {
	local dockerfile="$1"
	local context="$2"
	local docker_image="$3"

	build_dependency $dockerfile

	if is_image_built $docker_image; then
		return 0
	fi

	echo "Building $docker_image"
	docker image build --build-arg docker_registry=$docker_reg --tag $docker_image -f $dockerfile $context
	docker image push $docker_image
}

function build_image_versions() {
	local name="$1"
	local dockerfile="$name/Dockerfile"
	local context="$name"
	local docker_image="$docker_reg/$name:$default_version"

	case $name in
	"sep-keycloak")
		dockerfile="sirma-platform/keycloak-integration/Dockerfile"
		context="$(dirname $dockerfile)"
		;;
	"sirma-platform")
		;&
	"sep-base-wildfly")
		dockerfile="sirma-platform/platform/Dockerfile.base"
		context="$(dirname $dockerfile)"
		docker_image="$docker_reg/sep-base-wildfly:$default_version"
		;;
	esac

	if [ -f "$dockerfile" ]; then
		build_image "$dockerfile" "$context" "$docker_image"
	else
		for image_version in `ls $name`; do
			local context="$name/$image_version"

			build_image "$context/Dockerfile" "$context" "$docker_reg/$name:$image_version"
		done
	fi

}

function build_artefacts() {
	echo "Building artefacts"

	docker container run --rm -v $script_dir/http-proxy:/go/src/git.sirmaplatform.com/stash/seip/http-proxy -w /go/src/git.sirmaplatform.com/stash/seip/http-proxy instrumentisto/dep ensure -v
	docker container run --rm -v $script_dir/http-proxy:/go/src/git.sirmaplatform.com/stash/seip/http-proxy -w /go/src/git.sirmaplatform.com/stash/seip/http-proxy golang:1.14 go build -v -o rootfs/usr/local/bin/http-proxy-linux-amd64 cli/main.go

	docker container run --rm -it -v $temp_dir/m2:/root/.m2 -v $script_dir/content-preview:/data -w /data maven:3.3-jdk-8 mvn clean package

	for dep in excel-streaming-reader zimbra-soap-client; do
		docker container run --rm -it -v $temp_dir/m2:/root/.m2 -v $script_dir/sirma-platform/$dep:/data -w /data maven:3.3-jdk-8 mvn clean install
	done

	docker container run --rm -it -v $temp_dir/m2:/root/.m2 -v $script_dir/sirma-platform/eai:/data -w /data odinuge/maven-javafx:3-jdk-8 mvn clean install -Dkeystore.password="123456" -Dkeystore.alias=myorg -Dkeystore.path=/data/self-signed.jks -DskipTests=true clean install
	docker container run --rm -it -v $temp_dir/m2:/root/.m2 -v $script_dir/sirma-platform:/data -w /data maven:3.3-jdk-8 mvn -N install
	docker container run --rm -it -v $temp_dir/m2:/root/.m2 -v $script_dir/sirma-platform/bom:/data -w /data maven:3.3-jdk-8 mvn install
	docker container run --rm -it -v $temp_dir/m2:/root/.m2 -v $script_dir/sirma-platform:/data -w /data maven:3.3-jdk-8 mvn \
	        -Duser.timezone=UTC \
	        -DtrimStackTrace=false \
	        -DskipTests=true \
	        -T 1C install

	docker container run --rm -it -v $temp_dir/m2:/root/.m2 -v $script_dir/content-preview:/data -w /data maven:3.3-jdk-8 mvn clean package
	docker container run --rm -it -v $temp_dir/m2:/root/.m2 -v $script_dir/cs-wildfly:/data -w /data maven:3.3-jdk-8 mvn clean package
	docker container run --rm -it -v $temp_dir/m2:/root/.m2 -v $script_dir/ocr:/data -w /data maven:3.3-jdk-8 mvn clean package
	docker container run --rm -it -v $temp_dir/m2:/root/.m2 -v $script_dir/sep-alfresco/alfresco-emf-integration:/data -w /data maven:3.6-jdk-8  mvn -B clean package
	docker container run --rm -it -v $temp_dir/m2:/root/.m2 -v $script_dir/sep-alfresco/alfresco-emf-integration:/data -w /data maven:3.6-jdk-8 mvn -B install

	docker container run --rm -i -e JSPM_USERNAME=$JSPM_USERNAME -e  JSPM_TOKEN=$JSPM_TOKEN -v $script_dir/sep-ui:/data -w /data node:6 bash <<-EOF
		export PATH="/data/node_modules/.bin:$PATH"
		npm install
		npm rebuild node-sass
		jspm config registries.github.auth $(echo "console.log(Buffer.from(encodeURIComponent('$JSPM_USERNAME') + ':' +encodeURIComponent('$JSPM_TOKEN')).toString('base64'))" | node)
		jspm install
		gulp compile
		jspm install --dev
		gulp dist
		chown -R 1000:1000 .
	EOF
}

exec &> >(tee "$log_file")
echo "Build log will be written to $log_file"

build_artefacts

for name in `ls`; do
	if [[ -f $name ]]; then
		continue
	fi

	build_image_versions $name
done
