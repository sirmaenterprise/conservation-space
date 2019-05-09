import angular from 'angular';
import application from 'app/app';
import 'common/translate';
import 'common/libs';
import routerConfig from 'adapters/router/router';
import {Configuration} from 'common/application-config';
import 'security/authentication-interceptor';
import 'services/interceptors/http-error-interceptor';
import 'services/interceptors/active-requests-counter-interceptor';
import 'services/interceptors/request-headers-update-interceptor';
import jstz from 'jstz';
import 'services/logging/extended-exception-handler';
import {AuthenticationService} from 'security/authentication-service';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {RestClient} from 'services/rest-client';
import {PluginsService} from 'services/plugin/plugins-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {TranslateService} from 'services/i18n/translate-service';
import {UserService} from 'security/user-service';
import {JwtAuthenticator} from 'security/jwt/jwt-authenticator';
import {KeycloakAuthenticator} from 'security/keycloak/keycloak-authenticator';
import {UrlUtils} from 'common/url-utils';
import Keycloak from 'keycloak-js';

let keycloakAdapter;

application.run([TranslateService.name, UserService.name, AuthenticationService.name, RestClient.name, WindowAdapter.name, Configuration.name, PluginsService.name, PromiseAdapter.name, JwtAuthenticator.name, KeycloakAuthenticator.name, '$compile', '$rootScope',
  function (translateService, userService, authenticationService, restClient, windowAdapter, configuration, pluginsService, promiseAdapter, jwtAuthenticator, keycloakAuthenticator, $compile, $rootScope) {
    if (keycloakAdapter) {
      keycloakAuthenticator.init(keycloakAdapter);
      authenticationService.init(keycloakAuthenticator);
    } else {
      authenticationService.init(jwtAuthenticator);
    }

    if (authenticationService.authenticate()) {
      UrlUtils.removeQueryParam(windowAdapter.window, AuthenticationService.TOKEN_REQUEST_PARAM);

      configureRestClient(restClient);

      bootstrapApp(pluginsService, promiseAdapter, configuration, userService, translateService, $compile, $rootScope);
    }
  }]);

function bootstrapApp(pluginsService, promiseAdapter, configuration, userService, translateService, $compile, $rootScope) {
  pluginsService.loadPluginServiceModules('eventbus.global', 'component');

  let currentUser;
  // First, load all required stuff before invoking of any application code
  promiseAdapter.all([
    configuration.load(),
    userService.getCurrentUser()
  ]).then((data) => {
    currentUser = data[1];
    return bootstrapServices(pluginsService, promiseAdapter);
  }).then(() => {
    let language = currentUser.language;
    if (!language) {
      language = configuration.get(Configuration.SYSTEM_LANGUAGE);
    }
    translateService.changeLanguage(language);

    insertLayout($compile, $rootScope);

    if (!application.$rootScope.$$phase) {
      application.$rootScope.$digest();
    }
  });
}

/**
 * Collects all services registered under the "bootstrap-services" extension point and initializes them.
 *
 * <b>IMPORTANT</b>: Those services should not perform heavy performance tasks because it will slow the application's
 * start up.
 */
function bootstrapServices(pluginsService, promiseAdapter) {
  return pluginsService.loadPluginServiceModules('bootstrap-services', 'component').then((services) => {
    return promiseAdapter.all(Object.keys(services).map((serviceName) => {
      return services[serviceName].initialize();
    }));
  });
}

/**
 * Inserts the main DOM element in which other components of the application are initialized.
 */
function insertLayout($compile, $scope) {
  $(document.body).append('<div id="layout" extension-point="layout"></div>');
  $compile(document.getElementById('layout'))($scope);
}

function configureRestClient(restClient) {
  restClient.configure({
    'headers': {
      'Accept': 'application/vnd.seip.v2+json, application/json',
      'Timezone-Offset': new Date().getTimezoneOffset(),
      'Timezone': jstz.determine().name()
    }
  });
}

// Router should be injected in order to get the instance created
application.run(['$rootScope', '$state', function ($rootScope, $state) {
  $rootScope.$state = $state;
}]);

let routes = PluginRegistry.get('route');

application.config(routerConfig(application, routes));

application.config(['$urlRouterProvider', function ($urlRouterProvider) {
  $urlRouterProvider.otherwise('/userDashboard');
}]);

// Added to enable debug info like the $watch counter plugin for Chrome
// Will be removed in production
application.config(['$compileProvider', function ($compileProvider) {
  if (!window.prod) {
    $compileProvider.debugInfoEnabled(true);
    System.import('common/lib/debug/dom-elements-observer');
  }
}]);

angular.element(document).ready(function () {

  if (hasJwtParam()) {
    return bootstrapAngular();
  }

  // init keycloak before angular bootstrap to avoid issues with broken url parameters
  initKeycloakAdapter()
    .then(bootstrapAngular)
    .catch(console.error);

});

function hasJwtParam() {
  return window.location.href.indexOf(AuthenticationService.TOKEN_REQUEST_PARAM + '=') !== -1;
}

function bootstrapAngular() {
  UrlUtils.replaceUrl(window, decodeURIComponent(window.location.href));

  return angular.bootstrap(document.body, [application.name], {
    strictDi: true
  });
}

function initKeycloakAdapter() {
  keycloakAdapter = new Keycloak({
    url: '/auth',
    realm: getTenantParam() || KeycloakAuthenticator.MASTER_TENANT,
    clientId: KeycloakAuthenticator.CLIENT_ID
  });
  return keycloakAdapter.init({onLoad: KeycloakAuthenticator.LOGIN_REQUIRED});
}

function getTenantParam() {
  if (window.location.href.indexOf(KeycloakAuthenticator.TENANT + '=') !== -1) {
    let tenant = UrlUtils.getParameter(window.location, KeycloakAuthenticator.TENANT);
    UrlUtils.removeQueryParam(window, KeycloakAuthenticator.TENANT);
    localStorage.setItem(KeycloakAuthenticator.TENANT, tenant);
    return tenant;
  }
  return localStorage.getItem(KeycloakAuthenticator.TENANT);
}

export default application;