const fs = require('fs');
const url = require('url');
const http = require('http');
const https = require('https');
const uuid = require('uuid/v4');

const config = require('./config');

module.exports = (link) => {
  const filename = `${config.dirs.output}/${uuid()}.pdf`;

  const parsedLink = url.parse(link);
    const backendOpts = {
      protocol: parsedLink.protocol,
      host: parsedLink.hostname,
      port: parsedLink.port,
      path: parsedLink.path,
      rejectUnauthorized: false,
      method: 'GET'
    };

  const backend = backendOpts.protocol === 'https:' ? https : http

  return new Promise((resolve, reject) => {
    const stream = fs.createWriteStream(filename).on('error', reject);

    backend.get(backendOpts, (response) => {
      response
        .on('error', reject)
        .on('end', () => resolve(filename))
        .pipe(stream);
    });
  });
};
