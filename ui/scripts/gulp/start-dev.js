'use strict';

let spawnSync = require('child_process').spawnSync;
let fs = require('fs');
let path = require('path');
let _ = require('lodash');

const defaultDevConf = {
  reinstall: false,
  backend: {
    proto: 'http',
    host: 'localhost',
    port: '8080',
    context: '/emf'
  }
};

let rootdir = path.resolve(__dirname + '/../..');
let devConfFile = __dirname + '/dev.conf.json';
let devConf = defaultDevConf;

if (fs.existsSync(devConfFile)) {
  devConf = _.defaultsDeep(require(devConfFile), devConf);
}

fs.writeFileSync(devConfFile, JSON.stringify(devConf, null, 2));

module.exports = (gulp) => {
  gulp.task('start-dev', () => {
    let opt = {
      cwd: rootdir,
      stdio: [process.stdin, process.stdout, process.stderr]
    };

    if (devConf.reinstall) {
      console.log('Installing npm dependencies...')
      spawnSync('npm', ['install', '--progress=false'], opt);

      console.log('Installing jspm dependencies...')
      spawnSync(`${rootdir}/node_modules/.bin/jspm`, ['install'], opt);
    }

    let backend = `${devConf.backend.proto}://${devConf.backend.host}:${devConf.backend.port}${devConf.backend.context}`;
    spawnSync(`${rootdir}/node_modules/.bin/gulp`, ['dev', `--backend=${backend}`], opt);
  });
};