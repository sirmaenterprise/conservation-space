const path = require('path');

const projectDir = path.resolve(__dirname, '../');

module.exports = {
  dirs: {
    root: projectDir,
    output: process.env.EXPORT_OUTPUT_DIR || path.join(projectDir, 'output')
  },

  logging: {
    level: process.env.EXPORT_LOG_LEVEL || 'error',
    file: process.env.EXPORT_LOG_FILE || path.join(projectDir, 'logs', 'export.json')
  },

  server: {
    host: process.env.EXPORT_HOST || '',
    port: process.env.EXPORT_PORT || 8080
  },

  exporter: {
    chromium: process.env.CHROMIUM_PATH || '/usr/bin/chromium-browser',
    viewport: {
      width: process.env.EXPORT_VIEWPORT_WIDTH || 1360,
      height: process.env.EXPORT_VIEWPORT_HEIGHT || 1020
    },
    timeout: 120000
  }
};
