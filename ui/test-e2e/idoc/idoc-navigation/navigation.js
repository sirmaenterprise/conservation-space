'use strict';

var SandboxPage = require('../../page-object').SandboxPage;

class NavigationSandboxPage extends SandboxPage {

  open() {
    super.open('/sandbox/idoc/idoc-navigation/');
  }

  toggleNavigation() {
    var button = $('#toggleNavigation');
    browser.wait(EC.elementToBeClickable(button), DEFAULT_TIMEOUT);
    button.click();
  }

  collapseFirstHeading() {
    var button = $('.toc-icon');
    browser.wait(EC.elementToBeClickable(button), DEFAULT_TIMEOUT);
    button.click();
  }

  getContentArea() {
    return $('#mockEditor');
  }

  getHeading2() {
    return this.getContentArea().$('h2');
  }
}

module.exports = {
  NavigationSandboxPage: NavigationSandboxPage
};