const mkdirp = require('mkdirp');

const config = require('./lib/config');
const server = require('./lib/server');

mkdirp.sync(config.dirs.output);

server.listen(config.server.port, config.server.host, () => {
  console.log(`export service started with config: ${JSON.stringify(config, null, 2)}`);
});
