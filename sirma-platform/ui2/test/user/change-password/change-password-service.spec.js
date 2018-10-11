import {ChangePasswordService} from 'user/change-password/change-password-service';
import {DialogService} from 'components/dialog/dialog-service';
import {PromiseStub} from 'test/promise-stub'
import {StatusCodes} from 'services/rest/status-codes';

describe('ChangePasswordService', () => {

  let changePasswordService;
  let dialogService;
  let notificationService;
  let userService;

  beforeEach(() => {
    dialogService = getDialogServiceMock();
    notificationService = getNotificationServiceMock();
    userService = getUserServiceMock();
    changePasswordService = new ChangePasswordService(dialogService, notificationService, getTranslateServiceMock(), userService);
  });

  describe('openDialog()', () => {
    it('should invoke dialog service', () => {
      changePasswordService.openDialog();
      expect(dialogService.create.calledOnce).to.be.true;
    });
  });

  describe('getDialogConfiguration()', () => {
    it('should build correct dialog configuration', () => {
      let dialogConfiguration = changePasswordService.getDialogConfiguration();

      expect(dialogConfiguration.showHeader).to.be.true;
      expect(dialogConfiguration.header).to.exist;
      expect(dialogConfiguration.buttons[0].id).to.equal(DialogService.OK);
      expect(dialogConfiguration.buttons[0].disabled).to.be.true;
      expect(dialogConfiguration.buttons[1].id).to.equal(DialogService.CANCEL);
      expect(dialogConfiguration.buttons[1].dismiss).to.be.true;
    });

    it('should invoke a handler if change button is pressed', () => {
      changePasswordService.changePasswordHandler = sinon.spy();

      let dialogConfiguration = changePasswordService.getDialogConfiguration();
      let onButtonClick = dialogConfiguration.buttons[0].onButtonClick;
      onButtonClick('', {}, {});

      expect(changePasswordService.changePasswordHandler.calledOnce).to.be.true;
    });
  });

  describe('afterFormValidation()', () => {
    it('should enable button after successful form validation', () => {
      let button = {
        disabled: true
      };
      let data = [{
        isValid: true
      }];

      changePasswordService.afterFormValidation(button, data);
      expect(button.disabled).to.be.false;
    });

    it('should disable button after unsuccessful form validation', () => {
      let button = {
        disabled: false
      };
      let data = [{
        isValid: false
      }];

      changePasswordService.afterFormValidation(button, data);
      expect(button.disabled).to.be.true;
    });
  });

  describe('changePasswordHandler()', () => {
    it('should dismiss dialog and show success notification on successful password change', () => {
      dialogService = getDialogServiceMock();
      notificationService = getNotificationServiceMock();
      userService = getUserServiceMock(StatusCodes.SUCCESS);

      changePasswordService = new ChangePasswordService(dialogService, notificationService, getTranslateServiceMock(), userService);

      let changePasswordDialog = getChangePasswordDialog();
      let dialogConfig = getDialogConfig();

      changePasswordService.changePasswordHandler(changePasswordDialog, dialogConfig);
      expect(userService.changePassword.calledOnce).to.be.true;
      expect(notificationService.success.calledOnce).to.be.true;
      expect(dialogConfig.dismiss.calledOnce).to.be.true;
    });

    it('should show error notification on bad request response', () => {
      dialogService = getDialogServiceMock();
      notificationService = getNotificationServiceMock();
      userService = getUserServiceMock(StatusCodes.BAD_REQUEST, true);

      changePasswordService = new ChangePasswordService(dialogService, notificationService, getTranslateServiceMock(), userService);

      let changePasswordDialog = getChangePasswordDialog();
      let dialogConfig = getDialogConfig();

      changePasswordService.changePasswordHandler(changePasswordDialog, dialogConfig);
      expect(userService.changePassword.calledOnce).to.be.true;
      expect(notificationService.error.calledOnce).to.be.true;
      expect(dialogConfig.dismiss.called).to.be.false;
    });

    it('should show error notification on server error response', () => {
      dialogService = getDialogServiceMock();
      notificationService = getNotificationServiceMock();
      userService = getUserServiceMock(StatusCodes.SERVER_ERROR, true);
      let translateService = getTranslateServiceMock();

      changePasswordService = new ChangePasswordService(dialogService, notificationService, translateService, userService);

      let changePasswordDialog = getChangePasswordDialog();
      let dialogConfig = getDialogConfig();

      changePasswordService.changePasswordHandler(changePasswordDialog, dialogConfig);
      expect(userService.changePassword.calledOnce).to.be.true;
      expect(notificationService.error.calledOnce).to.be.true;
      expect(translateService.translateInstant.calledOnce).to.be.true;
      expect(dialogConfig.dismiss.called).to.be.false;
    });

    it('should show custom message error in notification on wrong password', () => {
      dialogService = getDialogServiceMock();
      notificationService = getNotificationServiceMock();
      userService = getUserServiceMockWithMessages(StatusCodes.BAD_REQUEST, true);
      let translateService = getTranslateServiceMock();

      changePasswordService = new ChangePasswordService(dialogService, notificationService, translateService, userService);

      let changePasswordDialog = getChangePasswordDialog();
      let dialogConfig = getDialogConfig();

      changePasswordService.changePasswordHandler(changePasswordDialog, dialogConfig);
      expect(userService.changePassword.calledOnce).to.be.true;
      expect(notificationService.error.calledOnce).to.be.true;
      expect(translateService.translateInstant.calledOnce).to.be.true;
      expect(dialogConfig.dismiss.called).to.be.false;
    });

    it('should show error message from response in notification on backend validation', () => {
      dialogService = getDialogServiceMock();
      notificationService = getNotificationServiceMock();
      userService = getUserServiceMockWithMessages(StatusCodes.BAD_REQUEST);
      let translateService = getTranslateServiceMock();

      changePasswordService = new ChangePasswordService(dialogService, notificationService, translateService, userService);

      let changePasswordDialog = getChangePasswordDialog();
      let dialogConfig = getDialogConfig();

      changePasswordService.changePasswordHandler(changePasswordDialog, dialogConfig);
      expect(userService.changePassword.calledOnce).to.be.true;
      expect(notificationService.error.calledOnce).to.be.true;
      expect(translateService.translateInstant.called).to.be.false;
      expect(dialogConfig.dismiss.called).to.be.false;
    });
  });

});

function getDialogServiceMock() {
  return {
    create: sinon.spy()
  }
}

function getNotificationServiceMock() {
  return {
    success: sinon.spy(),
    error: sinon.spy()
  }
}

function getTranslateServiceMock() {
  return {
    translateInstant: sinon.spy(() => {
      return 'translated';
    })
  }
}

function getUserServiceMock(state, reject) {
  return {
    changePassword: sinon.spy(() => {
      if (reject) {
        return PromiseStub.reject({
          status: state,
          data: {
          }
        });
      }
      return PromiseStub.resolve({status: state});
    })
  }
}

function getUserServiceMockWithMessages(state, wrongPassword) {
  return {
    changePassword: sinon.spy(() => {
      if (wrongPassword) {
        return PromiseStub.reject({
          status: state,
          data: {
            messages: {
              passwordWrongMessage: 'wrong password'
            }
          }
        });
      }
      return PromiseStub.reject({
        status: state,
        data: {
          messages: {
            passwordValidationMessage: 'invalid new password'
          }
        }
      });
    })
  }
}

function getChangePasswordDialog() {
  return {
    changePasswordDialog: {
      getCurrentPassword: sinon.spy(() => {
        return '123';
      }),
      getNewPassword: sinon.spy(() => {
        return '321';
      })
    }
  }
}

function getDialogConfig() {
  return {
    dismiss: sinon.spy()
  }
}