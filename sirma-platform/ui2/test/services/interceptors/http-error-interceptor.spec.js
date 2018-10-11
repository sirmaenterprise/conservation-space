import {HttpErrorInterceptor} from 'services/interceptors/http-error-interceptor';
import {NotificationService} from 'services/notification/notification-service';
import {StatusCodes} from 'services/rest/status-codes';
import {stub} from 'test/test-utils';

describe('HttpErrorInterceptor', function () {

  describe('responseError', function () {
    it('should reject the http promise', function (done) {
      let notificationService = stub(NotificationService);
      let interceptor = new HttpErrorInterceptor(stubNgInjector(notificationService), stubAuthenticationService(true));

      let isRejected;
      let rejectedPromise = interceptor.responseError(new Promise(function (resolve, reject) {
      }));
      done();
      rejectedPromise.then(
        function (result) {
          isRejected = false;
        },
        function (err) {
          isRejected = true;
        });
      expect(isRejected).to.be.true;
      expect(notificationService.error.calledOnce).to.be.true;
    });

    it('should invoke the notificationService', function () {
      let notificationService = stub(NotificationService);
      let interceptor = new HttpErrorInterceptor(stubNgInjector(notificationService), stubAuthenticationService(true));

      let rejection = {
        statusText: 'Please try again'
      };

      interceptor.responseError(rejection).catch(() => {
      });
      expect(notificationService.error.calledOnce).to.be.true;
    });

    it('should re-authenticate if rejected with 401 (Unauthorized)', function () {
      let notificationService = stub(NotificationService);
      let authenticationServiceStub = stubAuthenticationService(false);
      let interceptor = new HttpErrorInterceptor(stubNgInjector(notificationService), authenticationServiceStub);

      let rejection = {
        statusText: 'Unauthorized',
        status: StatusCodes.UNAUTHORIZED
      };
      interceptor.responseError(rejection).catch(() => {
      });
      expect(authenticationServiceStub.removeToken).to.be.calledOnce;
      expect(authenticationServiceStub.authenticate).to.be.calledOnce;
    });

    it('should skip intercepting on passed config', function () {
      let notificationService = stub(NotificationService);
      let authenticationServiceStub = stubAuthenticationService(false);
      let interceptor = new HttpErrorInterceptor(stubNgInjector(notificationService), authenticationServiceStub);

      let rejection = {
        config: {
          skipInterceptor: true
        },
        statusText: 'Conflict',
        status: StatusCodes.CONFLICT
      };
      interceptor.responseError(rejection).catch(() => {
      });

      expect(authenticationServiceStub.authenticate).to.not.have.been.called;
      expect(notificationService.error).to.not.have.been.called;
    });

    it('should skip intercepting on passed function as config', function () {
      let notificationService = stub(NotificationService);
      let authenticationServiceStub = stubAuthenticationService(false);
      let interceptor = new HttpErrorInterceptor(stubNgInjector(notificationService), authenticationServiceStub);

      let rejection = {
        config: {
          skipInterceptor: (rejection) => {
            return rejection.status === StatusCodes.CONFLICT;
          }
        },
        statusText: 'Conflict',
        status: StatusCodes.CONFLICT
      };

      interceptor.responseError(rejection).catch(() => {
      });

      expect(authenticationServiceStub.authenticate).to.not.have.been.called;
      expect(notificationService.error).to.not.have.been.called;
    });

    it('should return a generic message when the response does not have a message', () => {
      let notificationService = stub(NotificationService);
      let interceptor = new HttpErrorInterceptor(stubNgInjector(notificationService), stubAuthenticationService(true));

      let rejection = {
        data: {},
        status: -1,
        statusText: ""
      };
      interceptor.responseError(rejection);
      expect(notificationService.error.getCall(0).args[0].message).to.equals('The operation cannot be processed. Please contact the system administrator');
    });

    it('should not display notification if the user is not authenticated', () => {
      let notificationService = stub(NotificationService);
      let interceptor = new HttpErrorInterceptor(stubNgInjector(notificationService), stubAuthenticationService(false));

      let rejection = {
        data: null,
        status: -1,
        statusText: 'Test Status Text'
      };
      interceptor.responseError(rejection);
      expect(notificationService.error.calledOnce).to.be.false;
    });
  });

});

function stubAuthenticationService(authenticated) {
  return {
    isAuthenticated: () => {
      return authenticated;
    },
    removeToken: sinon.spy(),
    authenticate: sinon.spy()
  };
}

function stubNgInjector(injected) {
  return {
    get: () => injected
  };
}