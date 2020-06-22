import {HttpInterceptor, Inject} from 'app/app';
import {AuthenticationService} from 'security/authentication-service';
import {AUTHORIZATION} from 'services/rest/http-headers';

// A reference to 'this' context is stored here because interceptor functions are
// stored with no context when angular builds the interceptors chain.
// see: http://stackoverflow.com/questions/28638600/angularjs-http-interceptor-class-es6-loses-binding-to-this
let _self;

/**
 * Interceptor which adds authorization header on every request made by angular's http client.
 */
@HttpInterceptor
@Inject(AuthenticationService)
export class AuthenticationInterceptor {

  constructor(authenticationService) {
    this.authenticationService = authenticationService;
    _self = this;
  }

  request(config) {
    return _self.authenticationService.buildAuthHeader().then(authHeaderValue => {
      config.headers = config.headers || {};
      config.headers[AUTHORIZATION] = authHeaderValue;
      return config;
    });
  }

}
