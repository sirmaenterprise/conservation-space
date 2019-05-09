const express = require('express');
const bodyParser = require('body-parser');
const fs = require('fs');

const download = require('./downloader');
const compare = require('./comparer');
const logger = require('./logger');

const router = express.Router()
const app = express()
  .use(bodyParser.json({type: 'application/vnd.seip.v2+json'}))
  .use(router);

router
  .get('/health', (req, res) => {
  	res.status(200).end();
  })
  .route('/compare')
    .post((req, res) => {

      Promise.all([download(req.body.first), download(req.body.second)])
        .then(files => {
          return compare(files[0], files[1]);
        })
        .then(result => {
          res.writeHead(200, {
            'Content-Type': 'application/pdf',
            'Content-Length': fs.statSync(result)['size']
          });

          fs.createReadStream(result)
            .pipe(res)
            .on('end', () => res.end());
        })
        .catch(err => {
          logger.error(err.toString())

          let status = err.constructor.name === 'TimeoutError' ? 503 : 500
          res.writeHead(status, {
          	'Content-Type': 'text/plain'
          });

          res.write(err.toString());
          res.end();
        });
    });

module.exports = app;
