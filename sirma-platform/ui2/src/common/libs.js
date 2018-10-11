import 'lodash';
import angular from 'angular';
import application from 'app/app';
import 'angular-animate';
import 'angular-loading-bar';
import 'angular-sanitize';
import 'twbs/bootstrap-sass';
import 'angular-loading-bar/src/loading-bar.css!';

var libsModule = angular.module('libs', ['angular-loading-bar', 'ngSanitize', 'ngAnimate']);

// register the libs module with the main application module
application.requires.push(libsModule.name);

libsModule.config(['cfpLoadingBarProvider',
  function (cfpLoadingBarProvider) {
    cfpLoadingBarProvider.includeSpinner = false;
  }
]);

//configure angular-loader-bar
libsModule.run(['$rootScope', 'cfpLoadingBar',
  function ($rootScope, cfpLoadingBar) {
    $rootScope.$on('$stateChangeStart', function () {
      cfpLoadingBar.start();
    });

    $rootScope.$on('$stateChangeSuccess', function () {
      cfpLoadingBar.complete();
    });
  }
]);

libsModule.config(['$animateProvider', function ($animateProvider) {
  // Allow ng-animate only for elements having the following class.
  $animateProvider.classNameFilter(new RegExp('animated'));
}]);

/* Needed because in ES6 modules env the editor doesn't know its base path.
 * Path must be set before ckeditor loads.
 * Fixed with @link http://docs.ckeditor.com/#!/api/CKEDITOR-property-basePath
 */
if (!window.CKEDITOR_BASEPATH) {
  window.CKEDITOR_BASEPATH = '/common/lib/ckeditor/';
}

export default libsModule;