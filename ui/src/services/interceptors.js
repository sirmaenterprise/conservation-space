import {Inject,HttpInterceptor} from 'app/app';
import {NotificationService} from 'services/notification/notification-service';
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
@Inject(NotificationService, AuthenticationService)
export class HttpErrorInterceptor {

  constructor(notificationService, authenticationService) {
    this.notificationService = notificationService;
    this.authenticationService = authenticationService;
    _self = this;
  }

  responseError(rejection) {
    if (rejection.status === StatusCodes.UNAUTHORIZED) {
      _self.authenticationService.removeToken();
      _self.authenticationService.authenticate();

      return Promise.resolve(rejection);
    }

    if (rejection.config && rejection.config.skipInterceptor
      && (!_.isFunction(rejection.config.skipInterceptor) || rejection.config.skipInterceptor(rejection))) {
      // config passed by service which wants to process errors itself
      // this way errors can be processed by invokers
      return Promise.reject(rejection);
    }

    let message = '';
    if (rejection.data && rejection.data.message) {
      message = rejection.data.message;
    } else if (rejection.statusText) {
      message = rejection.statusText + '<br /><b>Http status:</b>' + rejection.status;
    }

    if (rejection.status === StatusCodes.ABORTED && !message) {
      message = 'A problem with the request has occured.'
    }

    // displaying errors when not authenticated causes is annoying
    if (_self.authenticationService.isAuthenticated()) {
      _self.notificationService.error({
        message: message
      });
    }

    return Promise.reject(rejection);
  }

}
