"use strict";

var SANDBOX_URL = '/sandbox/user/change-password/';
var Dialog = require('../../components/dialog/dialog');
var SandboxPage = require('../../page-object').SandboxPage;

const CURRENT_PASSWORD_ID = '#currentPassword';
const NEW_PASSWORD_ID = '#newPassword';
const CONFIRMATION_PASSWORD_ID = '#confirmNewPassword';

class ChangePasswordSandbox extends SandboxPage {

  open() {
    super.open(SANDBOX_URL);
    var button = $('.container .btn');
    browser.wait(EC.visibilityOf(button), DEFAULT_TIMEOUT);
  }

  openDialog() {
    $('.btn').click();
    var dialog = new ChangePassword($('.modal-dialog'));
    dialog.waitUntilOpened();
    return dialog;
  }

}

class ChangePassword extends Dialog {

  constructor(element) {
    super(element);
  }

  enterPasswordForElement(element, password) {
    this.waitForElementToBePresent(element);
    element.click();
    return element.sendKeys(password);
  }

  waitForElementToBeEnabled(element) {
    browser.wait(() => {
      return element.isEnabled();
    }, DEFAULT_TIMEOUT);
  }

  waitForElementToBeDisabled(element) {
    browser.wait(EC.not(() => {
      return element.isEnabled();
    }), DEFAULT_TIMEOUT);
  }

  waitForElementToBePresent(element) {
    browser.wait(EC.presenceOf(element), DEFAULT_TIMEOUT);
    browser.wait(EC.elementToBeClickable(element), DEFAULT_TIMEOUT);
  }

  get currentPassword() {
    return this.element.$('input' + CURRENT_PASSWORD_ID);
  }

  get newPassword() {
    return this.element.$('input' + NEW_PASSWORD_ID);
  }

  get newPasswordConfirmation() {
    return this.element.$('input' + CONFIRMATION_PASSWORD_ID);
  }

}

module.exports.ChangePasswordSandbox = ChangePasswordSandbox;
module.exports.ChangePassword = ChangePassword;