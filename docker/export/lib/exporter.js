const puppeteer = require('puppeteer');
const fs        = require('fs');
const uuid      = require('uuid/v4');

const config = require('./config');

class TimeoutError extends Error {
  constructor(message) {
    super(message)
  }
}

module.exports = (url, params = {}) => {
  return new Promise(async (resolve, reject) => {
    let id = uuid();
    let filename = `${config.dirs.output}/${id}.pdf`;
    let browser = null;
    let timeoutId = null;

    let timeout = params.timeout || config.exporter.timeout;
    let pageFormat = params.pageFormat || 'A4';

    try {
      timeoutId = setTimeout(async () => {
        reject(new TimeoutError(`unable to export in ${timeout / 1000} seconds.`))

        if (browser) {
          await browser.close();
          browser = null;
        }
      }, timeout);

      browser = await puppeteer.launch({
        executablePath: config.exporter.chromium,
        ignoreHTTPSErrors: true,
        dumpio: true,
        args: [
          `--user-data-dir=${config.dirs.output}/${id}.chromium`,

          // we need to be able to add capabilities or security profile to services (and in compose file) to remove the --no-sandbox parameter
          // https://github.com/moby/moby/issues/25885
          // https://github.com/moby/moby/issues/25209
          '--no-sandbox'
        ]
      })

      const page = await browser.newPage();
      page.setDefaultNavigationTimeout(timeout);
      await page.setViewport(config.exporter.viewport);

      await page.goto(url, {
        waitUntil: 'networkidle2'
      });
      await page.waitForFunction('window.status === "export-ready"', {timeout});

      await page.pdf({
        path: filename,
        format: pageFormat
      });

      resolve(fs.createReadStream(filename));
    } catch (err) {
      console.log(err);
      reject(err);
    } finally {
      if (browser) {
        await browser.close();
      }

      if (timeoutId) {
        clearTimeout(timeoutId);
      }
    }
  });
}
