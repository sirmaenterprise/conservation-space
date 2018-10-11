#!/bin/bash
set -e

APP_PATH=/seip-temp
PROFILE=$1
SCRIPTS_DIR=$APP_PATH/docker/build/scripts

echo "Copy the code to a temp directory"
cp -r /seip $APP_PATH

export GEOMETRY="$SCREEN_WIDTH""x""$SCREEN_HEIGHT""x""$SCREEN_DEPTH"
Xvfb $DISPLAY -screen 0 $GEOMETRY -extension RANDR &

$SCRIPTS_DIR/build.sh $PROFILE $APP_PATH /seip/reports