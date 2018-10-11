//Inspired by https://github.com/lookfirst/systemjs-seed/

// coverage with sourcemaps - https://github.com/gotwarlost/istanbul/issues/212
// sonarqube - http://blog.akquinet.de/2014/11/25/js-test-coverage/
// https://medium.com/@gunnarlium/es6-code-coverage-with-babel-jspm-karma-jasmine-and-istanbul-2c1918c5bb23

// preprocess the scss files first in order to be used by the js later

module.exports = function (config) {
  config.set({

    // base path that will be used to resolve all patterns (eg. files, exclude)
    basePath: '',

    // The maximum boot-up time allowed for a browser to start and connect to Karma; Default is 60000
    captureTimeout: 120000,
    // How long does Karma wait for a browser to reconnect; Default is 2000
    browserDisconnectTimeout: 10000,
    // The maximum number of tries a browser will attempt in the case of a disconnection; Default is 0
    browserDisconnectTolerance: 1,
    // How long will Karma wait for a message from a browser before disconnecting from; Default is 10000
    browserNoActivityTimeout: 180000,

    // frameworks to use
    // available frameworks: https://npmjs.org/browse/keyword/karma-adapter

    // mocha - http://mochajs.org/#getting-started
    // chai - http://chaijs.com/api/bdd/
    // sinon - http://sinonjs.org/docs/
    // fixture - https://github.com/billtrik/karma-fixture
    frameworks: ['jspm', 'mocha', 'chai-as-promised', 'sinon-chai', 'fixture'],

    customLaunchers: {
      Chrome_without_sandbox: {
        base: 'Chrome',
        flags: ['--no-sandbox'] // with sandbox it fails under Docker
      }
    },

    // list of files / patterns to load in the browser
    jspm: {
      config: 'config.js',
      loadFiles: ['test/**/*.js', 'test/**/*.html', 'test/dummy.css'],
      serveFiles: ['build-instrumented/**/*'],
      paths: {
        '*.css': '/base/test/dummy.css',
        'promise-stub': '/base/test/promise-stub.js',
        'test-utils': '/base/test/test-utils.js'
      }
    },

    proxies: {
      '/jspm_packages': '/base/jspm_packages',
      '/base/test': '/base/test',
      '/base': '/base/build-instrumented',
      '/base/app/': '/base/test/app/'
    },

    // list of files to exclude
    exclude: [],

    // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
    preprocessors: {
      // (these files will be instrumented by Istanbul)
      'build-instrumented': ['sourcemap', 'coverage'],
      'test/**/*.html': ['html2js']
    },
    // test results reporter to use
    // possible values: 'dots', 'progress'
    // available reporters: https://npmjs.org/browse/keyword/karma-reporter
    reporters: ['mocha', 'coverage'],
    // web server port
    port: process.env.TESTS_PORT || 9876,

    // enable / disable colors in the output (reporters and logs)
    colors: false,

    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_INFO,

    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: false,

    // start these browsers
    // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
    browsers: ['Chrome'],

    // Continuous Integration mode
    // if true, Karma captures browsers, runs the tests and exits
    singleRun: false,

    coverageReporter: {
      instrumenterOptions: {
        istanbul: {noCompact: true}
      },
      reporters: [{
        type: 'json',
        dir: 'reports',
        subdir: 'unit-coverage'
      }, {
        type: 'lcov',
        dir: 'reports',
        subdir: 'unit-coverage'
      }]
    }

  });
};
