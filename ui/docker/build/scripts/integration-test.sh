#!/bin/bash
set +e

cd $1

TEST_ARGS="--browser=$2"

if [[ ${SELENIUM_ADDRESS} ]]; then
  sed -i 's/directConnect: true/directConnect: false/g' $1/protractor.conf.js
  TEST_ARGS="$TEST_ARGS --seleniumAddress=$SELENIUM_ADDRESS --baseUrl=http://$HOST_ADDRESS"
fi

if [[ ${SELENIUM_THREADS} ]]; then
  TEST_ARGS="$TEST_ARGS --threads=$SELENIUM_THREADS"
fi

rm -rf $1/reports/screenshots

echo "Run integration tests on $2"
gulp e2e --coverage=true $TEST_ARGS

RETURN_CODE=$?
SCREENSHOTS_DEST_DIR=$3/screenshots

if [[ $RETURN_CODE != 0 ]]; then
echo "Copy protractor screenshots to $SCREENSHOTS_DEST_DIR"
rm -rf $SCREENSHOTS_DEST_DIR
cp -rp $1/reports/screenshots $SCREENSHOTS_DEST_DIR
fi

echo "Integration tests exited with code: $RETURN_CODE"

exit $RETURN_CODE