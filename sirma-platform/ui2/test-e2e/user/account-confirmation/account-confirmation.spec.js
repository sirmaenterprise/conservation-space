let AccountConfirmationSandbox = require('./account-confirmation').AccountConfirmationSandbox;
let Notification = require('../../components/notification').Notification;

describe('AccountConfirmation', () => {

  let page = new AccountConfirmationSandbox();
  let accountConfirmation;

  it('should have password fields and finish button', () => {
    openSandbox();

    let passwordField = accountConfirmation.getPasswordField();
    let repeatPasswordField = accountConfirmation.getConfirmPasswordField();

    expect(passwordField.isDisplayed()).to.eventually.be.true;
    expect(passwordField.getAttribute('type')).to.eventually.equal('password');

    expect(repeatPasswordField.isDisplayed()).to.eventually.be.true;
    expect(repeatPasswordField.getAttribute('type')).to.eventually.equal('password');

    expect(accountConfirmation.getFinishButton().isDisplayed()).to.eventually.be.true;
  });

  it('should disable finish button on initial load', () => {
    openSandbox();

    expect(accountConfirmation.getFinishButton().isEnabled()).to.eventually.be.false;
  });

  it('should enable finish button when the form is valid', () => {
    openSandbox();

    accountConfirmation.setPassword('myPassword');
    accountConfirmation.setConfirmPassword('myPassword');
    accountConfirmation.setCaptcha('answer');

    expect(accountConfirmation.getFinishButton().isEnabled()).to.eventually.be.true;
  });

  it('should not enable finish button when the form is not valid', () => {
    openSandbox();

    accountConfirmation.setPassword('myPassword');
    accountConfirmation.setConfirmPassword('123456');

    expect(accountConfirmation.getFinishButton().isEnabled()).to.eventually.be.false;
  });

  it('should hide form and show message when confirmation link is expired', () => {
    openSandbox(true);

    accountConfirmation.setPassword('myPassword');
    accountConfirmation.setConfirmPassword('myPassword');
    accountConfirmation.setCaptcha('answer');
    accountConfirmation.submitForm();

    new Notification().waitUntilOpened();
    accountConfirmation.expiredLinkMessagePresent();
    expect(accountConfirmation.isFormPresent()).to.eventually.be.false;
  });

  function openSandbox(expired) {
    page.open(expired);
    accountConfirmation = page.getAccountConfirmation();
  }

});