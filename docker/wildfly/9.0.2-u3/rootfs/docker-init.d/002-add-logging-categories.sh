#!/usr/bin/env bash

set -eu

if [ -z "${WILDFLY_LOGGING_CATEGORIES:-}" ]; then
	return
fi

sed 's|<resolve-parameter-values>false</resolve-parameter-values>|<resolve-parameter-values>true</resolve-parameter-values>|' -i $JBOSS_HOME/bin/jboss-cli.xml

for category in $WILDFLY_LOGGING_CATEGORIES; do
	IFS='=' read -r -a pair <<< "$category"

	echo "adding logging category ${pair[0]} with level ${pair[1]}"
	$JBOSS_HOME/bin/jboss-cli.sh -Dcategory="${pair[0]}" -Dlevel="${pair[1]}" --file=$JBOSS_HOME/bin/add-logging-category.cli
done

sed 's|<resolve-parameter-values>true</resolve-parameter-values>|<resolve-parameter-values>false</resolve-parameter-values>|' -i $JBOSS_HOME/bin/jboss-cli.xml

chown -R $DOCKER_USER:$DOCKER_USER $JBOSS_HOME/standalone $JBOSS_HOME/bin/jboss-cli.xml
sync
