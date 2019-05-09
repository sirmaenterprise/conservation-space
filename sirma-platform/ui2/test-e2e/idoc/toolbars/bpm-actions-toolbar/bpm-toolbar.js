'use strict';

class BpmToolbar {

  constructor() {
    this.selector = '.bpm-actions-section';
    this.element = $(this.selector);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getWorkflow() {
    return this.getElement('.bpm-workflow');
  }

  getButtons() {
    return this.getElement('.bpm-button');
  }

  getElement(selector) {
    return this.element.$(selector);
  }

  isToolbarPresent() {
    return this.element.isPresent();
  }
}

module.exports = BpmToolbar;