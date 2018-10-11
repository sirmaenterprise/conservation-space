import {AccountConfirmation} from 'user/account-confirmation/account-confirmation';
import {ResourceRestService} from 'services/rest/resources-service';
import {TranslateService} from 'services/i18n/translate-service';
import {AuthenticationService} from 'services/security/authentication-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {NotificationService} from 'services/notification/notification-service';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('AccountConfirmation', () => {

  let accountConfirmation;
  let resourceService;
  let authenticationService;
  let translateService;
  let eventbus;
  let windowAdapter;
  let notificationService;

  beforeEach(() => {
    resourceService = mockResourceService();
    authenticationService = stub(AuthenticationService);
    translateService = stub(TranslateService);
    eventbus = stub(Eventbus);
    windowAdapter = stub(WindowAdapter);
    notificationService = stub(NotificationService);

    accountConfirmation = new AccountConfirmation(resourceService, authenticationService, translateService, eventbus,
      windowAdapter, notificationService);
  });

  describe('redirectToHomeIfParamsMissing', () => {
    it('should navigate to home page if code is missing', () => {
      accountConfirmation.username = 'user';
      accountConfirmation.tenant = 'tenant';

      accountConfirmation.redirectToHomeIfParamsMissing();
      expect(windowAdapter.navigate.calledOnce).to.be.true;
    });

    it('should navigate to home page if username is missing', () => {
      accountConfirmation.code = 'code';
      accountConfirmation.tenant = 'tenant';

      accountConfirmation.redirectToHomeIfParamsMissing();
      expect(windowAdapter.navigate.calledOnce).to.be.true;
    });

    it('should navigate to home page if tenant is missing', () => {
      accountConfirmation.code = 'code';
      accountConfirmation.username = 'user';

      accountConfirmation.redirectToHomeIfParamsMissing();
      expect(windowAdapter.navigate.calledOnce).to.be.true;
    });
  });

  describe('getCaptcha', () => {
    it('should invoke service for retrieving captcha', () => {
      accountConfirmation.code = 'code';
      accountConfirmation.tenant = 'tenant';

      accountConfirmation.getCaptcha();

      expect(resourceService.getCaptcha.calledOnce).to.be.true;
      expect(resourceService.getCaptcha.calledWith(accountConfirmation.code, accountConfirmation.tenant)).to.be.true;
    });
  });

  describe('finish', () => {
    it('should invoke resource service with proper args', () => {
      accountConfirmation.code = 'code';
      accountConfirmation.tenant = 'tenant';
      accountConfirmation.username = 'user';
      accountConfirmation.formConfig = {
        models: {
          validationModel: {
            newPassword: {
              value: '123456'
            },
            captcha: {
              value: 'answer'
            }
          }
        }
      };

      accountConfirmation.finish();

      expect(resourceService.confirmAccount.calledOnce).to.be.true;
      expect(resourceService.confirmAccount.calledWith(accountConfirmation.username, '123456', accountConfirmation.code,
        'answer', accountConfirmation.tenant)).to.be.true;
    });

    it('should clear field values when confirmation failed', () => {
      resourceService.confirmAccount.returns(PromiseStub.reject({
        data: {
          message: 'Error'
        }
      }));

      accountConfirmation.code = 'code';
      accountConfirmation.tenant = 'tenant';
      accountConfirmation.username = 'user';
      accountConfirmation.formConfig = {
        models: {
          validationModel: {
            newPassword: {
              value: '123456'
            },
            confirmPassword: {
              value: '654321'
            },
            captcha: {
              value: 'answer'
            }
          }
        }
      };

      accountConfirmation.finish();

      let validationModel = accountConfirmation.formConfig.models.validationModel;
      expect(validationModel.newPassword.value).to.be.empty;
      expect(validationModel.confirmPassword.value).to.be.empty;
      expect(validationModel.captcha.value).to.be.empty;
    });

    it('should retrieve new captcha when confirmation failed', () => {
      resourceService.confirmAccount.returns(PromiseStub.reject({
        data: {
          message: 'Error'
        }
      }));

      accountConfirmation.formConfig = {
        models: {
          validationModel: {
            newPassword: {
              value: '123456'
            },
            confirmPassword: {
              value: '654321'
            },
            captcha: {
              value: 'answer'
            }
          }
        }
      };

      accountConfirmation.finish();

      expect(resourceService.getCaptcha.calledOnce).to.be.true;
    });

    it('should show error notification when confirmation failed', () => {
      resourceService.confirmAccount.returns(PromiseStub.reject({
        data: {
          message: 'Error'
        }
      }));

      accountConfirmation.formConfig = {
        models: {
          validationModel: {
            newPassword: {
              value: '123456'
            },
            confirmPassword: {
              value: '654321'
            },
            captcha: {
              value: 'answer'
            }
          }
        }
      };

      accountConfirmation.finish();

      expect(notificationService.error.calledWith('Error')).to.be.true;
    });

    it('should have expired message when response contains flag', () => {
      resourceService.confirmAccount.returns(PromiseStub.reject({
        data: {
          message: 'Error',
          expired: true
        }
      }));

      accountConfirmation.formConfig = {
        models: {
          validationModel: {
            newPassword: {
              value: '123456'
            },
            confirmPassword: {
              value: '654321'
            },
            captcha: {
              value: 'answer'
            }
          }
        }
      };

      accountConfirmation.finish();

      expect(notificationService.error.calledWith('Error')).to.be.true;
      expect(accountConfirmation.expiredMessage).to.equal('Error');
    });
  });

  function mockResourceService() {
    let resourceService = stub(ResourceRestService);
    resourceService.getCaptcha.returns(PromiseStub.resolve({
      data: {}
    }));
    resourceService.confirmAccount.returns(PromiseStub.resolve());
    return resourceService;
  }

});