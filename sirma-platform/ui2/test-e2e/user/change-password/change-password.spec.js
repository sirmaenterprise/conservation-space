let ChangePasswordSandbox = require('./change-password').ChangePasswordSandbox;

describe('ChangePassword', () => {

  let page = new ChangePasswordSandbox();

  beforeEach(() => {
    page.open();
  });

  it('should open change password dialog', () => {
    let dialog = page.openDialog();
    expect(dialog.isPresent()).to.eventually.be.true;
  });

  it('should have buttons for changing password and canceling', () => {
    let dialog = page.openDialog();
    let changeButton = dialog.getOkButton();

    // should have a button for changing password and be disabled
    expect(changeButton.isDisplayed()).to.eventually.be.true;
    dialog.waitForElementToBeDisabled(changeButton);

    // should have a button for canceling changing password
    expect(dialog.getCancelButton().isDisplayed()).to.eventually.be.true;
  });

  it('should have password fields', () => {
    let dialog = page.openDialog();

    let currentPasswordField = dialog.getCurrentPasswordField();
    let newPasswordField = dialog.getNewPasswordField();
    let newPasswordConfirmationField = dialog.getNewPasswordConfirmationField();

    // should have password field for current password
    expect(currentPasswordField.getAttribute('type')).to.eventually.equal('password');

    // should have password field for new password
    expect(newPasswordField.getAttribute('type')).to.eventually.equal('password');

    // should have password field for new password confirmation
    expect(newPasswordConfirmationField.getAttribute('type')).to.eventually.equal('password');
  });

  it('should enable change button if form is valid', () => {
    let dialog = page.openDialog();
    dialog.enterPasswordForElement(dialog.getCurrentPasswordField(), '123456').then(() => {
      dialog.enterPasswordForElement(dialog.getNewPasswordField(), 'password').then(() => {
        dialog.enterPasswordForElement(dialog.getNewPasswordConfirmationField(), 'password').then(() => {
          dialog.waitForElementToBeEnabled(dialog.getOkButton());
        });
      });
    });
  });

  it('should disable change button if form is invalid', () => {
    let dialog = page.openDialog();
    dialog.enterPasswordForElement(dialog.getCurrentPasswordField(), '123456').then(() => {
      dialog.enterPasswordForElement(dialog.getNewPasswordField(), 'password').then(() => {
        dialog.waitForElementToBeDisabled(dialog.getOkButton());
      });
    });
  });

});