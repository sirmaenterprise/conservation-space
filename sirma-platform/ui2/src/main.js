import angular from 'angular';
import application from 'app/app';
import translateModule from 'common/translate';
import libsModule from'common/libs';
import routerConfig from 'adapters/router/router';
import {Configuration} from 'common/application-config';
import 'services/interceptors/http-error-interceptor';
import 'services/interceptors/active-requests-counter-interceptor';
import 'services/interceptors/request-headers-update-interceptor';
import jstz from 'jstz';
import {ExtendedExceptionHandler} from 'services/logging/extended-exception-handler';
import {AuthenticationService} from 'services/security/authentication-service';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {RestClient} from 'services/rest-client';
import {PluginsService} from 'services/plugin/plugins-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {TranslateService} from 'services/i18n/translate-service';
import {UserService} from 'services/identity/user-service';

application.run([TranslateService.name, UserService.name, AuthenticationService.name, RestClient.name, WindowAdapter.name, Configuration.name, PluginsService.name, PromiseAdapter.name, '$compile', '$rootScope',
  function (translateService, userService, authenticationService, restClient, windowAdapter, configuration, pluginsService, promiseAdapter, $compile, $rootScope) {
    if (windowAdapter.location.href.indexOf('/public') !== -1) {
      // if public page load public layout
      $(document.body).append('<div id="public-layout" extension-point="public-layout"></div>');
      return;
    } else if (authenticationService.authenticate()) {
      configureRestClient(authenticationService, restClient);
    } else {
      // don't bootstrap the application if the user is not authenticated
      return;
    }

    // on page refresh always check if there is token in the url, because the cookie for jwt token is not removed after logout,
    // and then after login the old cookie is still present which causes the previous check here authenticationService.authenticate()
    // to not pass, therefore not removing the jwt token from the url
    if (windowAdapter.location.href.indexOf(AuthenticationService.TOKEN_REQUEST_PARAM + '=') !== -1) {
      // change the browser url to "pure" url without request parameters (doesn't count for hashtag params) because
      // there are cases where the security token is append to the url as request param if there isn't a hash tag and
      // this causes the app to keep that request param for the entire session. In result the router appends the hashtag
      // after the request parameter. I.e. http://localhost/?jwt=something#/idoc/.

      var regex = new RegExp(AuthenticationService.TOKEN_REQUEST_PARAM + '=[^&#]+');
      var hash = windowAdapter.location.hash;
      hash = hash.replace(regex, '');

      // remove trailing ? or &
      var lastChar = hash.substr(-1);
      if (lastChar === "?" || lastChar === "&") {
        hash = hash.substring(0, hash.length - 1);
      }

      window.history.pushState({}, "", '/' + hash);
    }

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
  }]);

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

function configureRestClient(authenticationService, restClient) {
  restClient.configure({
    'headers': {
      'Authorization': 'Bearer ' + authenticationService.getToken(),
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

var routes = PluginRegistry.get('route');

application.config(routerConfig(application, routes));

application.config(['$urlRouterProvider', function ($urlRouterProvider) {
  $urlRouterProvider.otherwise("/userDashboard");
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
  return angular.bootstrap(document.body, [application.name], {
    strictDi: true
  });
});

export default application;