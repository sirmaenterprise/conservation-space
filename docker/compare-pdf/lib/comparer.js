const fs = require('fs');
const { spawn } = require('child_process');
const uuid = require('uuid/v4');

const config = require('./config');

const spawnConfig = {
  env: process.env,
  // use parent's in, out, err
  stdio: ['inherit', 'inherit', 'inherit']
};

class TimeoutError extends Error {
  constructor(message) {
    super(message);
  }
}

module.exports = (first, second) => {
  const result = `${config.dirs.output}/${uuid()}.pdf`;

  return new Promise((resolve, reject) => {
    let error = null;

    const child = spawn('xvfb-run', ['-a', 'diffpdf', first, second, result], spawnConfig)
      .on('error', err => error = err)
      .on('close', code => {
        if (code !== 0) {
          if (error === null) {
            error = new Error(`process exited with non-zero code: ${code}`);
          }

          reject(error)
          return;
        }

        resolve(result);
      });

    setTimeout(() => {
      error = new TimeoutError(`timeout after ${config.compare.timeout / 1000} seconds`)
      child.kill();
    }, config.compare.timeout);
  });
};
