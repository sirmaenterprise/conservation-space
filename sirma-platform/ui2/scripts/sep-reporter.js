'use strict';

let Base = require('mocha/lib/reporters/base');
let Spec = require('mocha/lib/reporters/spec');
let AllureReporter = require('mocha-allure-reporter');
let Allure = require('allure-js-commons');
let Attachment = require('allure-js-commons/beans/attachment');
let util = require('allure-js-commons/util');
let writer = require('allure-js-commons/writer');
let _ = require('lodash');

module.exports = SEPReporter;

/**
 * Custom reporter for SEP that proxies Allure.
 *
 * It generates screen shots of failed tests and adds them in Allure's as attachments so they can be accessed later in the report.
 *
 * @param runner - the test runner
 * @param options - options provided to the runner
 */
function SEPReporter(runner, options) {
  // Call the Base mocha reporter, used for verbose logging
  Base.call(this, runner);

  let reporterOptions = _.defaultsDeep(options.reporterOptions, {
    screenshots: {
      passing: true,
      failing: true
    }
  });

  let screenshotPromises = [];

  let screenshotHandler = function (test, passed) {
    if (!reporterOptions.screenshots.passing && passed) {
      return;
    } else if (!reporterOptions.screenshots.failing && !passed) {
      return;
    }

    let currentSuite = allure._allure.getCurrentSuite();
    let currentStep = currentSuite.currentStep;

    screenshotPromises.push(browser.takeScreenshot().then((png) => {
      let attachmentBuffer = new Buffer(png, 'base64');
      let info = util.getBufferInfo(attachmentBuffer, 'image/png');
      let fileName = writer.writeBuffer('allure-results', attachmentBuffer, info.ext);
      let attachment = new Attachment('Screenshot', fileName, attachmentBuffer.length, info.mime);
      currentStep.addAttachment(attachment);
    }));
  };

  runner.on('pass', function (test) {
    screenshotHandler(test, true);
  });

  runner.on('fail', function (test) {
    screenshotHandler(test, false);
  });

  // Overriding to ensure all attachments are within the suite before saving it
  Allure.prototype.endSuite = (timestamp) => {
    let suite = allure._allure.getCurrentSuite();

    Promise.all(screenshotPromises).then(() => {
      suite.end(timestamp);
      if (suite.hasTests() && !suite.flushed) {
        writer.writeSuite('allure-results', suite);
        suite.flushed = true;
      }
    });

    allure._allure.suites.shift();
  };

  // Instantiate the proxied reporter
  new AllureReporter(runner, options);

  // Instantiate default reporter for console output
  new Spec(runner);
}
