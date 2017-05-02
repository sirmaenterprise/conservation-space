#!/usr/bin/env bash

set -eu

mvn_install() {
	local pom_dir="$1"
	shift

	(
		cd "$pom_dir"
		mvn -B -e -Duser.timezone=UTC -DtrimStackTrace=false clean install -U $@
	)
}

install_alfresco() {
	echo "Intalling Alfresco"

	mvn_install alfresco/alfresco-emf-integration -N
	mvn_install alfresco/alfresco-emf-integration/alfresco-integration-api
	mvn_install alfresco/alfresco-emf-integration
}

install_legacy_libs() {
	echo "Installing legacy libs"

	local legacy_dirs="legacy-libs/poms/sirma legacy-libs/poms/jee6 legacy-libs/itt-commons legacy-libs/codelist-utils legacy-libs/sirma-faces"
	for d in $legacy_dirs; do
		mvn_install $d -DskipTests=true
	done
}

install_tess4j() {
	echo "Installing tess4j"

	mvn_install "tess4j" -DskipTests=true
}

install_sep() {
	echo "Installing Sirma Enterprise Platform"
	mvn_install "sirma-platform/" -P unit-test
}

install_cs() {
	echo "Installing Conservation Space Deployment"
        mvn_install "cs-deployment/cs-parent/"
}

build_ui() {
	jspm config registries.github.auth c2lybWFpdHQtanNwbToxVFRQQHN3MHJk
	(
		cd ui
		npm install || true
		jspm install || true
		gulp compile
		PROJECT_VERSION=$(cat package.json | grep "version" | cut -d \: -f 2  | tr -d [,\"\ ])
		sudo docker build --rm --tag=docker-reg.sirmaplatform.com/seip-ui:$PROJECT_VERSION .
	)
}

install_alfresco
install_legacy_libs
install_tess4j
install_sep
install_cs
build_ui
