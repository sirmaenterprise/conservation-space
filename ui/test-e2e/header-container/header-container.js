'use strict';
var StaticInstanceHeader = require('../instance-header/static-instance-header/static-instance-header').StaticInstanceHeader;
var DropdownMenu = require('../components/dropdownmenu/dropdown-menu').DropdownMenu;
var SandboxPage = require('../page-object').SandboxPage;

const HEADER_CONTAINER_SANDBOX_PATH = '/sandbox/header-container';
const HEADER_CONTAINER_SELECTOR = '.header-container';
const ACTION_MENU_SELECTOR = '.actions-menu';
const INSTANCE_HEADER_SELECTOR = '.instance-header';

class HeaderContainer {

  getActionMenu() {
    return new DropdownMenu($(ACTION_MENU_SELECTOR));
  }

  getHeader() {
    return new StaticInstanceHeader($(INSTANCE_HEADER_SELECTOR));
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