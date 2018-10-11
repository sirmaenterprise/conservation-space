let SEPReporter = require('./scripts/sep-reporter.js');

// Reference config - https://github.com/angular/protractor/blob/master/docs/referenceConf.js
exports.config = {
  // The advantage of directly connecting to browser drivers is that your test scripts may start up and run faster.
  directConnect: true,

  framework: 'mocha',

  // A base URL for your application under test. Calls to protractor.get()
  // with relative paths will be prepended with this.
  baseUrl: 'http://localhost:7000',

  // Capabilities to be passed to the webdriver instance.
  capabilities: {
    'chromeOptions': {
      args: ['--no-sandbox', '--disable-gpu']
    },
    shardTestFiles: false,
    maxInstances: 1
  },

  // Pages may take longer on remote browser
  allScriptsTimeout: 60000,
  getPageTimeout: 60000,

  onPrepare: () => {
    EC = protractor.ExpectedConditions;

    DEFAULT_TIMEOUT = browser.params['timeout'];
    if (!DEFAULT_TIMEOUT) {
      DEFAULT_TIMEOUT = 60000;
    }

    chai = require('chai');
    chaiAsPromised = require('chai-as-promised');
    chai.use(chaiAsPromised);
    expect = chai.expect;

    let originalWait = browser.wait.bind(browser);
    browser.wait = function (condition, timeout, message) {
      if (!timeout) {
        throw new Error('Timeout should be provided');
      }
      originalWait(condition, timeout, message);
    };

    let browserGet = browser.get.bind(browser);
    let params = {
      coverage: browser.params['coverage'],
      protractor: 'true'
    };

    browser.driver.manage().window().maximize();

    // can be used for debugging in protractor tests as a normal browser console
    browser.log = function () {
      let args = Array.prototype.slice.call(arguments);
      browser.sleep(0).then(() => {
        console.log.apply(null, args);
      });
    };

    browser.get = function (url) {
      let hasAnchor = url.indexOf('#') !== -1;
      let baseUrl = url.substring(0, hasAnchor ? url.indexOf('#') : url.length);
      let anchors = '';
      if (hasAnchor) {
        anchors = url.substring(url.indexOf('#'));
      }
      if (baseUrl.indexOf('?') !== -1) {
        baseUrl = baseUrl.concat('&');
      } else {
        baseUrl = baseUrl.concat('?');
      }

      let paramsKeys = Object.keys(params);
      let paramsKeysLen = Object.keys(params).length;
      for (let i = 0; i < paramsKeysLen; i++) {
        if (params[paramsKeys[i]]) {
          baseUrl = baseUrl.concat(paramsKeys[i] + '=' + params[paramsKeys[i]]);
          if (i + 1 !== paramsKeysLen) {
            baseUrl = baseUrl.concat('&');
          }
        }
      }

      url = baseUrl + anchors;
      browserGet(url);
    };
  },

  onComplete: () => {
    console.log('Browser errors during tests:');
    browser.manage().logs().get('browser').then(function (browserLogs) {
      browserLogs.forEach(function (log) {
        if (log.level.value > 900) { // it's an error log
          console.log(log.message);
        }
      });
    });
  },

  plugins: [{
    path: 'node_modules/protractor-istanbul-plugin',
    outputPath: 'reports/e2e-coverage/coverage-build/',
    logAssertions: true
  }],

  // Spec patterns are relative to the configuration file location passed to protractor (in this example conf.js).
  // They may include glob patterns.
  specs: ['test-e2e/**/*.spec.js'],

  mochaOpts: {
    enableTimeouts: false,
    reporter: SEPReporter,
    reporterOptions: {
      screenshots: {
        passing: false,
        failing: true
      }
    }
  }
};
