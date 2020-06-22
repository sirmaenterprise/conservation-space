'use strict';
var TestUtils = require('../../test-utils');
var SandboxPage = require('../../page-object').SandboxPage;

const PAGE_URL = '/sandbox/idoc/sidebar';

class SidebarSandboxPage extends SandboxPage {
  open() {
    super.open(PAGE_URL);
  }

  getSidebar() {
    return new Sidebar($('.sidebar'));
  }
}

class Sidebar  {

  constructor(element) {
    this.element = element;
    this.expandButton = this.element.$('.hamburger > a');
    this.waitUntilOpened();
    return this;
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  isCollapsed() {
    return TestUtils.hasClass(this.element, 'collapsed');
  }

  toggleCollapse() {
    this.expandButton.click();
    return this;
  }

  getChildCount(selector) {
    return this.element.$$(selector).count();
  }

  /**
   * Sidebar's expanded/collapsed state is remembered in the session store. Clean it after each test
   */
  onDestroy() {
    browser.executeScript(`window.sessionStorage.removeItem('sep-sidebar-width');`);
    browser.executeScript(`window.sessionStorage.removeItem('sep-sidebar-collapsed');`);
  }
}

module.exports.Sidebar = Sidebar;
module.exports.SidebarSandboxPage = SidebarSandboxPage;
