/**
 * Bootstraps a sandbox page.
 *
 * @param configuration Object used to provide:
 * - paths - systemjs paths bindings - used to mock existing modules
 * - modules - modules to load using systemjs before bootstrapping the application
 */
function bootstrapSandbox(configuration) {
  var completeFn = function () {
  };
  var paths;
  if (isCoverageMode()) {
    paths = {
      "*": "build-instrumented/*.js"
    };
  } else {
    paths = {
      "*": "build/*.js",
      'services/logging/logger': 'sandbox/logger.stub.js'
    };
  }

  paths['sandbox/*'] = 'sandbox/*.js';

  // services stubs
  paths['services/rest/definition-service'] = 'sandbox/services/rest/definition-service.stub.js';
  paths['services/rest/instance-service'] = 'sandbox/services/rest/instance-service.stub.js';
  paths['services/rest/configurations-service'] = 'sandbox/services/rest/configurations-service.stub.js';
  paths['security/authentication-service'] = 'sandbox/security/authentication-service.stub.js';
  paths['services/rest/resources-service'] = 'sandbox/services/rest/resources-service.stub.js';
  paths['services/rest/search-service'] = 'sandbox/services/rest/search-service.stub.js';
  paths['services/rest/relationships-service'] = 'sandbox/services/rest/relationships-service.stub.js';
  paths['services/rest/template-service'] = 'sandbox/services/rest/template-service.stub.js';
  paths['services/rest/image-service'] = 'sandbox/services/rest/image-service.stub.js';
  paths['services/rest/comments-service'] = 'sandbox/services/rest/comments-service.stub.js';
  paths['services/rest/properties-service'] = 'sandbox/services/rest/properties-service.stub.js';
  paths['services/rest/permissions-service'] = 'sandbox/services/rest/permissions-service.stub.js';
  paths['services/rest/codelist-service'] = 'sandbox/services/rest/codelist-service.stub.js';
  paths['services/rest/models-service'] = 'sandbox/services/rest/models-service.stub.js';
  paths['services/rest/namespace-service'] = 'sandbox/services/rest/namespace-service.stub.js';
  paths['services/rest/upload-class-icons-service'] = 'sandbox/services/rest/upload-class-icons-service.stub.js';
  paths['services/rest/advanced-search-service'] = 'sandbox/services/rest/advanced-search-service.stub.js';
  paths['services/rest/actions-service'] = 'sandbox/services/rest/actions-service.stub.js';
  paths['services/help/help-service'] = 'sandbox/services/help/help-service.stub.js';
  paths['services/rest/bpm-service'] = 'sandbox/services/rest/bpm-service.stub.js';
  paths['services/create/create-panel-service'] = 'sandbox/services/create/create-panel-service.stub.js';
  paths['services/rest/role-management-service'] = 'sandbox/services/rest/role-management-service.stub.js';
  paths['security/user-service'] = 'sandbox/security/user-service.stub.js';
  paths['services/rest/eai-service'] = 'sandbox/services/rest/eai-service.stub.js';
  paths['services/rest/label-service'] = 'sandbox/services/rest/label-service.stub.js';
  paths['services/rest/audit-log-service'] = 'sandbox/services/rest/audit-log-service.stub.js';
  paths['services/rest/concept-service'] = 'sandbox/services/rest/concept-service.stub.js';
  paths['services/rest/mailbox-info-service'] = 'sandbox/services/rest/mailbox-info-service.stub.js';
  paths['services/rest/object-browser-service'] = 'sandbox/services/rest/object-browser-service.stub.js';
  paths['instance-header/headers-service'] = 'sandbox/services/rest/headers-service.stub.js';

  if (configuration.paths) {
    Object.keys(configuration.paths).forEach(function(key) {
      paths[key] = configuration.paths[key];
    });
  }

  let map = {};

  if (configuration.map) {
    Object.keys(configuration.map).forEach(function(key) {
      map[key] = configuration.map[key];
    });
  }

  useMinifiedLibs(paths);

  System.config({
    'paths': paths,
    'map': map
  });

  // help CKEDITOR properly load its resources in sandbox
  window.CKEDITOR_BASEPATH = '/build/common/lib/ckeditor/';

  var modulesToImport = ['app/app', 'common/translate', 'common/libs'];

  if (configuration.modules) {
    modulesToImport = modulesToImport.concat(configuration.modules);
  }

  var importPromises = modulesToImport.map(function (module) {
    return System.import(module);
  });

  Promise.all(importPromises).then(function () {
    // capture config and body for refresh
    window.bodyHtml = document.body.innerHTML;

    if (isProtractorTest()) {
      // fix for protractor - it doesn't work well with manually bootstrapped angular app
      window.name = 'NG_DEFER_BOOTSTRAP!' + window.name;
      document.body.setAttribute('ng-app', '');
    }

    // bootstrap the application
    angular.element(document).ready(function () {
      // only if other module has not bootstrapped it yet (e.g. main.js)
      if (!angular.element(document.body).scope()) {
        angular.bootstrap(document.body, ['app'], {
          strictDi: true
        });
        completeFn();
      }
    });
  });
  return new Promise(function (resolve) {
    completeFn = resolve;
  });
}

window.restartSandbox = function () {
  var $body = $(document.body);
  $body.empty();
  document.body.innerHTML = window.bodyHtml;
  var app = angular.module('app');
  var rootScope = $body.scope();

  // cleanup existing scopes
  for (var childScope = rootScope.$$childHead; childScope; childScope = childScope.$$nextSibling) {
    childScope.$destroy();
  }

  app.$compile(document.body)(rootScope);
  rootScope.$digest();
}

function isProtractorTest() {
  return eval(getUrlParams(window.location.href).baseUrl['protractor']);
}

function isCoverageMode() {
  return eval(getUrlParams(window.location.href).baseUrl['coverage']);
}

function getUrlParams(url) {
  var extractParamTokens = function (url) {
    var tokens = url.split('?');
    if (tokens.length > 1) {
      return tokens[1].split('&');
    }
    return {};
  };

  var extractParamTokensToMap = function (tokens) {
    var paramsMap = {};
    for (token in tokens) {
      var key = tokens[token].substring(0, tokens[token].indexOf('='));
      var value = tokens[token].substring(tokens[token].indexOf('=') + 1);
      paramsMap[key] = value;
    }
    return paramsMap;
  };

  var indexOfAnchor = url.indexOf('#');
  var baseUrl = url.substring(0, (indexOfAnchor > -1) ? indexOfAnchor : url.length);
  var anchorUrl = url.substring(indexOfAnchor);

  var baseUrlParamsTokens = extractParamTokens(baseUrl);
  var baseUrlParamsMap = extractParamTokensToMap(baseUrlParamsTokens);

  var anchorUrlParamsTokens = extractParamTokens(anchorUrl);
  var anchorUrlParamsMap = extractParamTokensToMap(anchorUrlParamsTokens);

  return {
    baseUrl: baseUrlParamsMap,
    anchorUrl: anchorUrlParamsMap
  };
}

function useMinifiedLibs(paths) {
  paths['github:components/jquery@*'] = 'jspm_packages/github/components/jquery@*/jquery.min.js';
  paths['github:twbs/bootstrap-sass@*'] = 'jspm_packages/github/twbs/bootstrap-sass@*/assets/javascripts/bootstrap.min.js';
  paths['github:angular/bower-angular@*'] = 'jspm_packages/github/angular/bower-angular@*/angular.min.js';
  paths['github:angular-ui/ui-router@*'] = 'jspm_packages/github/angular-ui/ui-router@*/angular-ui-router.min.js';
  paths['github:angular-translate/bower-angular-translate@*'] = 'jspm_packages/github/angular-translate/bower-angular-translate@*/angular-translate.min.js';
  paths['github:vakata/jstree@*'] = 'jspm_packages/github/vakata/jstree@*/dist/jstree.min.js';
  paths['github:vakata/jstree@*.css'] = 'jspm_packages/github/vakata/jstree@*.min.css';
  paths['github:Eonasdan/bootstrap-datetimepicker@*'] = 'jspm_packages/github/Eonasdan/bootstrap-datetimepicker@*/build/js/bootstrap-datetimepicker.min.js';
  paths['github:Eonasdan/bootstrap-datetimepicker@*.css'] = 'jspm_packages/github/Eonasdan/bootstrap-datetimepicker@*.min.css';
  paths['github:postaljs/postal.js@*'] = 'jspm_packages/github/postaljs/postal.js@*/postal.min.js';
  paths['github:johnny/jquery-sortable@*'] = 'jspm_packages/github/johnny/jquery-sortable@*/source/js/jquery-sortable-min.js';
  paths['github:CodeSeven/toastr@*'] = 'jspm_packages/github/CodeSeven/toastr@*/build/toastr.min.js';
  paths['github:CodeSeven/toastr@*toastr.css'] = 'jspm_packages/github/CodeSeven/toastr@*/build/toastr.min.css';

  paths['npm:select2@*'] = 'jspm_packages/npm/select2@*/js/select2.min.js';
  paths['npm:select2@*.css'] = 'jspm_packages/npm/select2@*.min.css';
  paths['npm:font-awesome@*.css'] = 'jspm_packages/npm/font-awesome@*.min.css';

  paths['npm:babel-core@*'] = 'jspm_packages/npm/babel-core@*/browser.min.js';
}
