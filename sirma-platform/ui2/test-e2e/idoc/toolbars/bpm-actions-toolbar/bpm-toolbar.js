"use strict";

class BpmToolbar {

  constructor() {
    this.selector = '.bpm-actions-section';
    this.el = $(this.selector);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.el), DEFAULT_TIMEOUT);
  }

  getWorkflow() {
    return this.getElement('.bpm-workflow');
  }

  getButtons() {
    return this.getElement('.bpm-button');
  }

  getElement(selector) {
    return this.el.$(selector);
  }

  isToolbarPresent() {
    return this.el.isPresent();
  }
}

module.exports = BpmToolbar;