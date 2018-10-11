const express     = require('express');
const bodyParser  = require('body-parser');
const fs          = require('fs');

const exporter  = require('./exporter');

const router = express.Router()
const app = express()
  .use(bodyParser.json({type: 'application/vnd.seip.v2+json'}))
  .use('/export', router);

router
  .get('/health', (req, res) => {
    res.status(200).end();
  })
  .route('/pdf')
    .post((req, res) => {
      exporter(req.body.url, req.body)
        .then((stream) => {
          res.writeHead(200, {
            'Content-Type': 'application/pdf'
          });

          stream
            .pipe(res)
            .on('end', () => res.end());
        })
        .catch((err) => {
          console.log(err)

          let status = err.constructor.name === 'TimeoutError' ? 503 : 500
          res.writeHead(status, {
            'Content-Type': 'text/plain'
          });

          res.write(err.toString());
          res.end();
        });
    });

module.exports = app;
