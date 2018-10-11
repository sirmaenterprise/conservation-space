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

  getContent() {
    return this.element.$('.toast-message');
  }

  close() {
    this.waitUntilOpened().click();
  }

  clickNoficationCloseButton() {
    this.element.$('.toast-close-button').click();
  }

}

module.exports.Notification = Notification;
