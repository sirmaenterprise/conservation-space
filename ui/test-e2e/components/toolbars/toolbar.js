"use strict";

class Toolbar {

  constructor(selector) {
    this.selector = selector;
    this.element = $(selector);
    this.waitUntilOpened();
  }

  waitUntilOpened() {
    browser.wait(EC.presenceOf(this.element), DEFAULT_TIMEOUT);
  }

  getSection(selector) {
    return this.element.$(selector);
  }

}

module.exports = Toolbar;