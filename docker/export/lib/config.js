const path = require('path');

const projectDir = path.resolve(__dirname, '../');

module.exports = {
  dirs: {
    root: projectDir,
    output: process.env.EXPORT_OUTPUT_DIR || path.join(projectDir, 'output')
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
