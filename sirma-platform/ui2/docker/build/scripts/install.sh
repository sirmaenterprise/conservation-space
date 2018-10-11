#!/bin/bash
set +e

cd $1

grep ".only(" -R test test-e2e
if [[ $? == 0 ]]; then
  echo "There are specs with .only(";
  exit 1;
fi

echo "Install npm packages"
rm -rf $1/node_modules/
test -d /node_modules && echo "Use node_modules cache" && cp -r /node_modules $1
if [[ ${NPM_REG} ]]; then
  echo "Usinng npm registry $NPM_REG"
  NPM_PARAMS="$NPM_PARAMS --registry $NPM_REG"
fi

TIMEFORMAT="NPM install finished after: %Rs"
npm set progress=false
time npm $NPM_PARAMS install

echo "Install jspm packages"
rm -rf $1/jspm_packages/
test -d /jspm_packages && echo "Use jspm_packages cache" && cp -r /jspm_packages $1
jspm install
jspm install --dev

echo "Install selenium drivers"
npm run postinstall
