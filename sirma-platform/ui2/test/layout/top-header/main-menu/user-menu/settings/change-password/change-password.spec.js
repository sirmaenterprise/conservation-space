import {ChangePassword} from 'layout/top-header/main-menu/user-menu/settings/change-password/change-password';
import {ChangePasswordService} from 'user/change-password/change-password-service';
import {stub} from 'test/test-utils';

describe('ChangePassword', () => {

  let changePassword;
  let changePasswordService;

  beforeEach(() => {
    changePasswordService = stub(ChangePasswordService);

    changePassword = new ChangePassword(changePasswordService);
  });

  describe('openDialog', () => {
    it('should invoke change password service', () => {
      changePassword.openDialog();

      expect(changePasswordService.openDialog.calledOnce).to.be.true;
    });
  });

});