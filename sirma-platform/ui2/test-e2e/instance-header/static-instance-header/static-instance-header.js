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

  getHeader() {
    return this.element.$('.instance-data');
  }

  getHeaderAsText() {
    return this.element.$('.instance-data').getText();
  }

  getIcon() {
    return this.element.$('.header-icon img');
  }

  getLockedLabel() {
    return this.element.$('.label-warning');
  }

  isClickable() {
    return this.element.$('.instance-link').click().then(() => {
      return true;
    }, () => {
      return false;
    });
  }
}
StaticInstanceHeader.COMPONENT_SELECTOR = '.instance-header';

module.exports = {
  StaticInstanceHeaderSandboxPage,
  StaticInstanceHeader
};