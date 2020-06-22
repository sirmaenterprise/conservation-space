import {AuthenticationInterceptor} from 'security/authentication-interceptor';
import {AuthenticationService} from 'security/authentication-service';
import {AUTHORIZATION} from 'services/rest/http-headers';
import {stub} from 'test/test-utils';
import {PromiseStub} from 'test/promise-stub';

describe('AuthenticationInterceptor', () => {

  let interceptor;
  let authenticationService;

  beforeEach(() => {
    authenticationService = stub(AuthenticationService);
    authenticationService.buildAuthHeader.returns(PromiseStub.resolve('Bearer token'));

    interceptor = new AuthenticationInterceptor(authenticationService);
  });

  describe('request()', () => {
    it('should add authorization header when request headers are undefined', () => {
      let config = {};
      interceptor.request(config);
      expect(config.headers).to.deep.equal({[AUTHORIZATION]: 'Bearer token'});
    });

    it('should add authorization header to existing headers for request', () => {
      let config = {
        headers: {
          'Content-Type': 'application/json'
        }
      };
      interceptor.request(config);
      expect(config.headers).to.deep.equal({'Content-Type': 'application/json', [AUTHORIZATION]: 'Bearer token'});
    });
  });

});