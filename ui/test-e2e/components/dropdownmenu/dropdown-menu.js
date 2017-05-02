'use strict';
var TestUtils = require('../../test-utils');

const SELECT_LOADER = '.loading';
const SELECT_MENU_DROPDOWN = '.dropdown-menu';
const SELECT_ITEM = '.menu-item';

class DropdownMenu {
  constructor(element) {
    this.triggerMenuButton = element;
  }

  open() {
    this.getTriggerButton().click();
    browser.wait(EC.visibilityOf(this.triggerMenuButton.$(SELECT_ITEM)), DEFAULT_TIMEOUT);
    return this;
  }

  hasLoadingIndicator() {
    this.getTriggerButton().click();
    browser.wait(EC.presenceOf($(SELECT_LOADER)), DEFAULT_TIMEOUT);
  }

  getTriggerButton() {
    return this.triggerMenuButton;
  }

  getActions() {
    return this.getActionContainer().all(by.css('li'));
  }

  getActionContainer() {
    return this.triggerMenuButton.$(SELECT_MENU_DROPDOWN);
  }

  showSubmenu() {
    browser.actions().mouseMove(this.triggerMenuButton.$('.dropdown-submenu')).perform();
  }

  getSubmenu() {
    return this.triggerMenuButton.$('.dropdown-submenu > .dropdown-menu');
  }

  /**
   * @returns action with style class 'active'
   */
  getActiveAction() {
    return this.getActions().filter(function (action) {
      return TestUtils.hasClass(action, 'active');
    }).first();
  }
}
module.exports.DropdownMenu = DropdownMenu;