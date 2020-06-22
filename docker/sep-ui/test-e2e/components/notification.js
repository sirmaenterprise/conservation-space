'use strict';

let TestUtils = require('../test-utils');

class Notification {

  constructor() {
    this.element = $('.toast');
  }

  waitUntilOpened() {
    browser.wait(EC.presenceOf(this.element), DEFAULT_TIMEOUT);
    return this;
  }

  getTitle() {
    return this.element.$('.toast-title').getText();
  }

  getMessage() {
    return this.element.$('.toast-message').getText();
  }

  isError() {
    return TestUtils.hasClass(this.element, 'toast-error');
  }

  isSuccess() {
    return TestUtils.hasClass(this.element, 'toast-success');
  }

  isWarning() {
    return TestUtils.hasClass(this.element, 'toast-warning');
  }

  getContent() {
    return this.element.$('.toast-message');
  }

  close() {
    this.waitUntilOpened().clickNoficationCloseButton();
  }

  clickNoficationCloseButton() {
    this.element.$('.toast-close-button').click();
    browser.wait(EC.invisibilityOf(this.element), DEFAULT_TIMEOUT);
    return this;
  }
}

module.exports.Notification = Notification;
