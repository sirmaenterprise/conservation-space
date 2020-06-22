'use strict';

let SANDBOX_URL = '/sandbox/user/change-password/';
let Dialog = require('../../components/dialog/dialog');
let SandboxPage = require('../../page-object').SandboxPage;

const CURRENT_PASSWORD_ID = '#currentPassword';
const NEW_PASSWORD_ID = '#newPassword';
const CONFIRMATION_PASSWORD_ID = '#confirmNewPassword';

class ChangePasswordSandbox extends SandboxPage {

  open() {
    super.open(SANDBOX_URL);
    let button = $('.container .btn');
    browser.wait(EC.visibilityOf(button), DEFAULT_TIMEOUT);
  }

  openDialog() {
    $('.btn').click();
    let dialog = new ChangePassword($('.modal-dialog'));
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
    browser.wait(() => element.isEnabled(), DEFAULT_TIMEOUT);
  }

  waitForElementToBeDisabled(element) {
    browser.wait(EC.not(() => element.isEnabled()), DEFAULT_TIMEOUT);
  }

  waitForElementToBePresent(element) {
    browser.wait(EC.presenceOf(element), DEFAULT_TIMEOUT);
    browser.wait(EC.elementToBeClickable(element), DEFAULT_TIMEOUT);
  }

  getCurrentPasswordField() {
    let field = this.element.$('input' + CURRENT_PASSWORD_ID);
    this.waitForElementToBePresent(field);
    return field;
  }

  getNewPasswordField() {
    let field = this.element.$('input' + NEW_PASSWORD_ID);
    this.waitForElementToBePresent(field);
    return field;
  }

  getNewPasswordConfirmationField() {
    let field = this.element.$('input' + CONFIRMATION_PASSWORD_ID);
    this.waitForElementToBePresent(field);
    return field;
  }

}

module.exports.ChangePasswordSandbox = ChangePasswordSandbox;
module.exports.ChangePassword = ChangePassword;