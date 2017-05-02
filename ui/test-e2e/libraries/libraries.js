'use strict';

var InstanceList = require('../instance/instance-list').InstanceList;
var SandboxPage = require('../page-object').SandboxPage;

class LibrariesSandboxPage extends SandboxPage {

  open() {
    super.open('/sandbox/libraries/');
  }

  getLibrariesPanel() {
    var librariesPanel = new LibrariesPanel($('.libraries'));
    librariesPanel.waitUntilOpened();
    return librariesPanel;
  }
}

class LibrariesPanel {
  constructor(element) {
    this.element = element;
    this.libs = new InstanceList(element.$('.search-results .instance-list'));
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }
}

module.exports = {
  LibrariesSandboxPage,
  LibrariesPanel
};