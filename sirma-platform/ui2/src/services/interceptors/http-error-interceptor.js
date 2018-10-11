import {Inject, HttpInterceptor, NgInjector} from 'app/app';
import {AuthenticationService} from 'services/security/authentication-service';
import {StatusCodes} from 'services/rest/status-codes';

import _ from 'lodash';

// A reference to 'this' context is stored here because interceptor functions are
// stored with no context when angular builds the interceptors chain.
// see: http://stackoverflow.com/questions/28638600/angularjs-http-interceptor-class-es6-loses-binding-to-this
let _self;

/**
 * An http interceptor that handles server errors and shows notification to the user.
 *
 * @author svelikov
 */
@HttpInterceptor
@Inject(NgInjector, AuthenticationService)
export class HttpErrorInterceptor {

  constructor($injector, authenticationService) {
    this.$injector = $injector;
    this.authenticationService = authenticationService;
    _self = this;
  }

  responseError(rejection) {
    if (rejection.status === StatusCodes.UNAUTHORIZED) {
      _self.authenticationService.removeToken();
      _self.authenticationService.authenticate();

      return Promise.reject(rejection);
    }

    // RestClient <- LoggingRestService <- Logger <- TranslateService <- HttpErrorInterceptor <- $http <- RestClient <- LabelRestService <- labelLoader

    if (rejection.config && rejection.config.skipInterceptor
      && (!_.isFunction(rejection.config.skipInterceptor) || rejection.config.skipInterceptor(rejection))) {
      // config passed by service which wants to process errors itself
      // this way errors can be processed by invokers
      return Promise.reject(rejection);
    }

    let message = '';
    if (rejection.data && rejection.data.message) {
      message = rejection.data.message;
    } else {
      message = 'The operation cannot be processed. Please contact the system administrator';
    }

    // displaying errors when not authenticated causes is annoying
    if (_self.authenticationService.isAuthenticated()) {
      // make sure to load the NotificationService at run time
      // due to circular dependency issue revealed from CMF-28057
      _self.$injector.get('NotificationService').error({
        message: message
      });
    }

    return Promise.reject(rejection);
  }

}