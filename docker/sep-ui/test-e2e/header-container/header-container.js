'use strict';
var StaticInstanceHeader = require('../instance-header/static-instance-header/static-instance-header').StaticInstanceHeader;
var DropdownMenu = require('../components/dropdownmenu/dropdown-menu').DropdownMenu;
var SandboxPage = require('../page-object').SandboxPage;

const HEADER_CONTAINER_SANDBOX_PATH = '/sandbox/header-container';
const HEADER_CONTAINER_SELECTOR = '.header-container';
const ACTION_MENU_SELECTOR = '.actions-menu';
const INSTANCE_HEADER_SELECTOR = '.instance-header';
const INSTANCE_HEADER_LINK_SELECTOR = '.instance-header a';

class HeaderContainer {

  constructor(element) {
    this.element = element;
  }

  getActionMenu() {
    return new DropdownMenu(this.element.$(ACTION_MENU_SELECTOR));
  }

  getHeader() {
    return new StaticInstanceHeader(this.element.$(INSTANCE_HEADER_SELECTOR));
  }

  // Provide the number of the returned element from the array you want to open
  openHeaderLink(linkNumber = 0) {
    return this.element.$$(INSTANCE_HEADER_LINK_SELECTOR).get(linkNumber).click();
  }
}

class HeaderContainerSandboxPage extends SandboxPage {
  open() {
    super.open(HEADER_CONTAINER_SANDBOX_PATH);
    this.waitUntilOpened();
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf($(HEADER_CONTAINER_SELECTOR)), DEFAULT_TIMEOUT);
  }

  getHeaderContainer() {
    return new HeaderContainer($(HEADER_CONTAINER_SELECTOR));
  }
}

module.exports = {
  HeaderContainer,
  HeaderContainerSandboxPage
};