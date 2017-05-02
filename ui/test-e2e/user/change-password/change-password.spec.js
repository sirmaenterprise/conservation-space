var ChangePasswordSandbox = require('./change-password').ChangePasswordSandbox;

describe('ChangePassword', ()=> {

  var page = new ChangePasswordSandbox();

  beforeEach(() => {
    page.open();
  });

  it('should open change password dialog', () => {
    var dialog = page.openDialog();
    expect(dialog.isPresent()).to.eventually.be.true;
  });

  it('should have buttons for changing password and canceling', () => {
    var dialog = page.openDialog();
    var changeButton = dialog.getOkButton();

    // should have a button for changing password and be disabled
    expect(changeButton.isDisplayed()).to.eventually.be.true;
    dialog.waitForElementToBeDisabled(changeButton);

    // should have a button for canceling changing password
    expect(dialog.getCancelButton().isDisplayed()).to.eventually.be.true;
  });

  it('should have password fields', () => {
    var dialog = page.openDialog();

    var currentPasswordField = dialog.currentPassword;
    var newPasswordField = dialog.newPassword;
    var newPasswordConfirmationField = dialog.newPasswordConfirmation;

    // should have password field for current password
    expect(currentPasswordField.isDisplayed()).to.eventually.be.true;
    expect(currentPasswordField.getAttribute('type')).to.eventually.equal('password');

    // should have password field for new password
    expect(newPasswordField.isDisplayed()).to.eventually.be.true;
    expect(newPasswordField.getAttribute('type')).to.eventually.equal('password');

    // should have password field for new password confirmation
    expect(newPasswordConfirmationField.isDisplayed()).to.eventually.be.true;
    expect(newPasswordConfirmationField.getAttribute('type')).to.eventually.equal('password');
  });

  it('should enable change button if form is valid', () => {
    var dialog = page.openDialog();
    dialog.enterPasswordForElement(dialog.currentPassword, '123456').then(() => {
      dialog.enterPasswordForElement(dialog.newPassword, 'password').then(() => {
        dialog.enterPasswordForElement(dialog.newPasswordConfirmation, 'password').then(() => {
          dialog.waitForElementToBeEnabled(dialog.getOkButton());
        });
      });
    });
  });

  it('should disable change button if form is invalid', () => {
    var dialog = page.openDialog();
    dialog.enterPasswordForElement(dialog.currentPassword, '123456').then(() => {
      dialog.enterPasswordForElement(dialog.newPassword, 'password').then(() => {
        dialog.waitForElementToBeDisabled(dialog.getOkButton());
      });
    });
  });

});