// Karma configuration
module.exports = function(config) {
  config.set({

    // base path, that will be used to resolve files and exclude
    basePath: '../js/',


    // frameworks to use
    frameworks: ['jasmine'],


    // list of files / patterns to load in the browser
    files: [
      'lib/jquery-1.7.2.min.js',
      'lib/angular.js',
      'lib/angular-mocks.js',
      'lib/jquery-ui.js',
      'lib/jquery-ui-timepicker-addon.js',
      'lib/lodash-2.4.1.js',
      'mocks/**/*.js',
      '../../../../../idoc-web/src/main/resources/META-INF/resources/js/widget-manager.js',
      '../../../main/resources/**/*.js',
      '../../../../../idoc-web/src/main/resources/META-INF/resources/js/idoc-common-filters.js',
      '../../../../../idoc-web/src/main/resources/META-INF/resources/js/idoc-common-services.js',
      '../../../../../idoc-web/src/main/resources/META-INF/resources/js/idoc-angular-app.js',
      '../../../../../idoc-web/src/main/resources/META-INF/resources/js/idoc-directives.js',
      '../../../../../idoc-web/src/main/resources/META-INF/resources/js/idoc-services.js',
      '../../../main/resources/**/template.html',
      'tests/**/*.spec.js'
    ],

    // list of files to exclude
    exclude: [
      
    ],
    
    // generate js files from html templates
    preprocessors: {
    	'../../../main/resources/**/*.js': 'coverage',
    	'../../../main/resources/**/*.html': 'ng-html2js'
    },
    
    plugins: [ 'karma-jasmine', 'karma-coverage', 'karma-phantomjs-launcher', 'karma-ng-html2js-preprocessor' ],

    ngHtml2JsPreprocessor: {        
        // or define a custom transform function
        cacheIdFromPath: function(filepath) {
        	var regex = /.*?idoc-widget\/src\/main\/resources\/META-INF\/resources/i;
        	return filepath.replace(regex, '/emf');
        },
        
        // A single module where all templates will be
        moduleName: 'widgetTemplates'
    },

    // test results reporter to use
    // possible values: 'dots', 'progress', 'junit', 'growl', 'coverage'
    reporters: ['dots', 'coverage'],
    
    coverageReporter: {
    	type : 'html',
    	// where to store the report
    	dir : 'coverage/'
	},

    // web server port
    port: 9877,


    // enable / disable colors in the output (reporters and logs)
    // if true in eclipse console prints get a bit messy, but still readable
    colors: false,


    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_DEBUG,


    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,


    // Start these browsers, currently available:
    // - Chrome
    // - ChromeCanary
    // - Firefox
    // - Opera
    // - Safari (only Mac)
    // - PhantomJS
    // - IE (only Windows)
    browsers: ['PhantomJS'],


    // If browser does not capture in given timeout [ms], kill it
    captureTimeout: 10000,


    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    // when running with maven this is always true
    singleRun: false
  });
};
