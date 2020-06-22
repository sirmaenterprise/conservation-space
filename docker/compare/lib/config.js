const path = require('path');

const projectDir = path.resolve(__dirname, '../');

module.exports = {
  dirs: {
    root: projectDir,
    output: process.env.COMPARE_OUTPUT_DIR || path.join(projectDir, '/output')
  },
  logging: {
    level: process.env.COMPARE_LOG_LEVEL || 'error',
    file: process.env.COMPARE_LOG_FILE || path.join(projectDir, 'logs', 'compare.json')
  },
  server: {
    host: process.env.COMPARE_HOST || '',
    port: process.env.COMPARE_PORT || 8080
  },
  compare: {
    timeout: process.env.COMPARE_TIMEOUT || 120000
  }
};
