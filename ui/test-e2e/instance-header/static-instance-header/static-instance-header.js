'use strict';

var SandboxPage = require('../../page-object').SandboxPage;

class StaticInstanceHeaderSandboxPage extends SandboxPage {
  open() {
    super.open('sandbox/instance-header/static-instance-header/');
  }
}

class StaticInstanceHeader {
  constructor(element) {
    this.element = element;
  }

  getHeaderAsText() {
    return this.element.$('.instance-data').getText();
  }

  getIcon() {
    return this.element.$('.instance-icon img');
  }

  isClickable() {
    return this.element.$('.instance-link').click().then(() => {
      return true;
    }, () => {
      return false;
    });
  }
}

module.exports = {
  StaticInstanceHeaderSandboxPage,
  StaticInstanceHeader
};