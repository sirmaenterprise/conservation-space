const config = require('./config');

const { createLogger, format, transports } = require('winston');
const { combine, timestamp, json } = format;

const logger = createLogger({
  level: config.logging.level,
  format: combine(
    timestamp(),
    json()
  ),
  defaultMeta: { service: 'export' },
  transports: [
    new transports.File({ filename: config.logging.file }),
    new transports.Console({
      format: format.simple()
    })
  ]
});

module.exports = logger;
