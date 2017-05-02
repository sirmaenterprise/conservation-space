import {HttpErrorInterceptor} from 'services/interceptors';
import {NotificationService, ERROR_TIMEOUT} from 'services/notification/notification-service';
import {StatusCodes} from 'services/rest/status-codes';

describe('HttpErrorInterceptor', function () {

  describe('responseError', function () {
    it('should reject the http promise', function (done) {
      var notificationService = new NotificationService();
      sinon.stub(notificationService, 'error', function () {
      });
      let interceptor = new HttpErrorInterceptor(notificationService, stubAuthenticationService(true));
      var isRejected;
      var rejectedPromise = interceptor.responseError(new Promise(function (resolve, reject) {
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
    });

    it('should invoke the notificationService', function () {
      var notificationService = new NotificationService();
      sinon.stub(notificationService, 'error', function () {
      });
      let interceptor = new HttpErrorInterceptor(notificationService, stubAuthenticationService(true));
      var rejection = {
        statusText: 'Please try again'
      };

      interceptor.responseError(rejection).catch(() => {
      });
      expect(notificationService.error.callCount).to.equal(1);
    });

    it('should re-authenticate if rejected with 401 (Unauthorized)', function () {
      let notificationService = new NotificationService();
      sinon.stub(notificationService, 'error', function () {
      });
      let authenticationServiceStub = sinon.stub();
      authenticationServiceStub.removeToken = sinon.spy();
      authenticationServiceStub.authenticate = sinon.spy();
      let interceptor = new HttpErrorInterceptor(notificationService, authenticationServiceStub);
      var rejection = {
        statusText: 'Unauthorized',
        status: StatusCodes.UNAUTHORIZED
      };
      interceptor.responseError(rejection).catch(() => {
      });
      expect(authenticationServiceStub.removeToken).to.be.calledOnce;
      expect(authenticationServiceStub.authenticate).to.be.calledOnce;
    });

    it('should skip intercepting on passed config', function () {
      let notificationService = new NotificationService();
      sinon.stub(notificationService, 'error', function () {
      });

      let authenticationServiceStub = sinon.stub();
      authenticationServiceStub.authenticate = sinon.spy();

      let interceptor = new HttpErrorInterceptor(notificationService, authenticationServiceStub);
      var rejection = {
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
      let notificationService = new NotificationService();
      sinon.stub(notificationService, 'error', function () {
      });

      let authenticationServiceStub = sinon.stub();
      authenticationServiceStub.authenticate = sinon.spy();

      let interceptor = new HttpErrorInterceptor(notificationService, authenticationServiceStub);
      var rejection = {
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

    it('should return a generic message when a response with status code -1 is returned', ()=> {
      let notificationService = new NotificationService();
      sinon.spy(notificationService, 'error');

      let interceptor = new HttpErrorInterceptor(notificationService, stubAuthenticationService(true));
      let rejection = {
        data: null,
        status: -1,
        statusText: ""
      };
      interceptor.responseError(rejection);
      expect(notificationService.error.getCall(0).args[0].message).to.equals('A problem with the request has occured.');
    });

    it('should not return a generic message', ()=> {
      let notificationService = new NotificationService();
      sinon.spy(notificationService, 'error');

      let interceptor = new HttpErrorInterceptor(notificationService, stubAuthenticationService(true));
      let rejection = {
        data: null,
        status: -1,
        statusText: 'Test Status Text'
      };
      interceptor.responseError(rejection);
      expect(notificationService.error.getCall(0).args[0].message).to.equals('Test Status Text<br /><b>Http status:</b>-1');
    })

    it('should not display notification if the user is not authenticated', () => {
      let notificationService = new NotificationService();
      sinon.spy(notificationService, 'error');

      let interceptor = new HttpErrorInterceptor(notificationService, stubAuthenticationService(false));
      let rejection = {
        data: null,
        status: -1,
        statusText: 'Test Status Text'
      };
      interceptor.responseError(rejection);
      expect(notificationService.error.callCount).to.equals(0);
    });
  });

});

function stubAuthenticationService(authenticated) {
  return {
    isAuthenticated: function () {
      return authenticated;
    }
  };
}