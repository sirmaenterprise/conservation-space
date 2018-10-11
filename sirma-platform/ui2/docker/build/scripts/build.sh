#!/bin/bash
set -e

PROFILE=$1
APP_PATH=$2
REPORTS_PATH=$3
SCRIPTS_DIR=$APP_PATH/docker/build/scripts
PROJECT_VERSION=$(cat $APP_PATH/package.json | grep "version" | cut -d \: -f 2  | tr -d [,\"\ ])

mkdir -p $REPORTS_PATH

$SCRIPTS_DIR/install.sh $APP_PATH

case $PROFILE in
  "pull-request")
    $SCRIPTS_DIR/compile.sh $APP_PATH $PROJECT_VERSION
    $SCRIPTS_DIR/integration-test.sh $APP_PATH chrome $REPORTS_PATH
    $SCRIPTS_DIR/sonar.sh $APP_PATH $PROJECT_VERSION
  ;;
  "deploy")
    $SCRIPTS_DIR/compile.sh $APP_PATH $PROJECT_VERSION
    $SCRIPTS_DIR/integration-test.sh $APP_PATH chrome $REPORTS_PATH
    $SCRIPTS_DIR/sonar.sh $APP_PATH $PROJECT_VERSION
    $SCRIPTS_DIR/package.sh $APP_PATH $PROJECT_VERSION
    $SCRIPTS_DIR/deploy.sh $APP_PATH $PROJECT_VERSION
    $SCRIPTS_DIR/cache-modules.sh $APP_PATH
  ;;
  "nightly")

  ;;
	*)
	echo "Unknown profile"
  exit 1;
	;;
esac