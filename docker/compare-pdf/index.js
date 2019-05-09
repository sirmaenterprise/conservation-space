const mkdirp = require('mkdirp');

const config = require('./lib/config');
const server = require('./lib/server');
const logger = require('./lib/logger');

mkdirp.sync(config.dirs.output);

server.listen(config.server.port, config.server.host, () => {
  logger.child({host: config.server.host, port: config.server.port}).info('service started');
});
