'use strict';

let SANDBOX_URL = '/sandbox/user/account-confirmation/?code=123&username=john&tenant=t.com';
let SANDBOX_EXPIRED_URL = '/sandbox/user/account-confirmation/?code=expired&username=john&tenant=t.com';
let SandboxPage = require('../../page-object').SandboxPage;
let PageObject = require('../../page-object').PageObject;

const NEW_PASSWORD_ID = '#newPassword';
const CONFIRM_PASSWORD_ID = '#confirmPassword';
const CAPTCHA_ID = '#captcha';

class AccountConfirmationSandbox extends SandboxPage {

  open(openExpiredUrl) {
    let sandboxUrl = SANDBOX_URL;
    if (openExpiredUrl) {
      sandboxUrl = SANDBOX_EXPIRED_URL;
    }
    super.open(sandboxUrl);

    browser.wait(EC.visibilityOf($(AccountConfirmation.COMPONENT_SELECTOR)), DEFAULT_TIMEOUT);
  }

  getAccountConfirmation() {
    let accountConfirmation = new AccountConfirmation($(AccountConfirmation.COMPONENT_SELECTOR));
    accountConfirmation.waitUntilOpened();
    return accountConfirmation;
  }

}

class AccountConfirmation extends PageObject {

  constructor(element) {
    super(element);
  }

  enterText(element, text) {
    this.waitForElementToBePresentAndClickable(element);
    element.click();
    element.sendKeys(text);
  }

  waitUntilOpened() {
    browser.wait(EC.presenceOf(this.element), DEFAULT_TIMEOUT);
    // wait for the form builder
    browser.wait(EC.presenceOf(this.element.$('.form-content')), DEFAULT_TIMEOUT);
  }

  setPassword(password) {
    this.enterText(this.getPasswordField(), password);
  }

  setConfirmPassword(password) {
    this.enterText(this.getConfirmPasswordField(), password);
  }

  setCaptcha(answer) {
    this.enterText(this.getCaptchaField(), answer);
  }

  getPasswordField() {
    let field = this.element.$('input' + NEW_PASSWORD_ID);
    this.waitForElementToBePresentAndClickable(field);
    return field;
  }

  getConfirmPasswordField() {
    let field = this.element.$('input' + CONFIRM_PASSWORD_ID);
    this.waitForElementToBePresentAndClickable(field);
    return field;
  }

  getCaptchaField() {
    let field = this.element.$('input' + CAPTCHA_ID);
    this.waitForElementToBePresentAndClickable(field);
    return field;
  }

  getFinishButton() {
    let button = this.element.$('.finish-btn');
    this.waitForElementToBePresent(button);
    return button;
  }

  submitForm() {
    let button = this.getFinishButton();
    this.waitForElementToBeClickable(button);
    button.click();
  }

  isFormPresent() {
    return this.element.$('.confirmation-form').isPresent();
  }

  expiredLinkMessagePresent() {
    this.waitForElementToBePresent(this.element.$('.expired-message'));
  }

  waitForElementToBePresentAndClickable(element) {
    this.waitForElementToBePresent(element);
    this.waitForElementToBeClickable(element);
  }

  waitForElementToBePresent(element) {
    browser.wait(EC.presenceOf(element), DEFAULT_TIMEOUT);
  }

  waitForElementToBeClickable(element) {
    browser.wait(EC.elementToBeClickable(element), DEFAULT_TIMEOUT);
  }

}

AccountConfirmation.COMPONENT_SELECTOR = '.account-confirmation';

module.exports.AccountConfirmationSandbox = AccountConfirmationSandbox;
module.exports.AccountConfirmation = AccountConfirmation;