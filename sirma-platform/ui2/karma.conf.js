//Inspired by https://github.com/lookfirst/systemjs-seed/

// coverage with sourcemaps - https://github.com/gotwarlost/istanbul/issues/212
// sonarqube - http://blog.akquinet.de/2014/11/25/js-test-coverage/
// https://medium.com/@gunnarlium/es6-code-coverage-with-babel-jspm-karma-jasmine-and-istanbul-2c1918c5bb23

// preprocess the scss files first in order to be used by the js later

module.exports = function (config) {
  config.set({

    // base path that will be used to resolve all patterns (eg. files, exclude)
    basePath: '',

    browserNoActivityTimeout: 100000,

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
        flags: ['--no-sandbox', '--headless'] // with sandbox it fails under Docker
      }
    },

    // list of files / patterns to load in the browser
    jspm: {
      config: 'config.js',
      loadFiles: ['test/**/*.js', 'test/**/*.html', 'test/dummy.css'],
      serveFiles: ['src/**/*'],
      paths: {
        '*.css': '/base/test/dummy.css',
        'promise-stub': '/base/test/promise-stub.js',
        'test-utils': '/base/test/test-utils.js'
      }
    },

    proxies: {
      '/jspm_packages': '/base/jspm_packages',
      '/base/test': '/base/test',
      '/base': '/base/src',
      '/base/app/': '/base/test/app/'
    },

    // list of files to exclude
    exclude: [],

    // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
    preprocessors: {
      // (these files will be instrumented by Istanbul)
      'src/!(common)/**/*.js': ['babel'],
      'src/common/!(lib)/*.js': ['babel'],
      'src/common/*.js': ['babel'],
      'test/**/*.html': ['html2js']
    },
    babelPreprocessor: {
      options: {
        sourceMap: 'inline',
        blacklist: ['useStrict'],
        modules: 'system',
        moduleIds: false,
        comments: true,
        compact: false,
        externalHelpers: true,
        loose: 'all',
        optional: [
          'es7.decorators',
          'es7.classProperties'
        ],
        auxiliaryCommentBefore: 'istanbul ignore next' // not working
      },
      sourceFileName: function (file) {
        return file.originalPath;
      }
    },

    // test results reporter to use
    // possible values: 'dots', 'progress'
    // available reporters: https://npmjs.org/browse/keyword/karma-reporter
    reporters: ['mocha'],
    // web server port
    port: 9876,

    // enable / disable colors in the output (reporters and logs)
    colors: true,

    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_INFO,

    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,

    // start these browsers
    // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
    browsers: ['Chrome'],

    // Continuous Integration mode
    // if true, Karma captures browsers, runs the tests and exits
    singleRun: false
  });
};
