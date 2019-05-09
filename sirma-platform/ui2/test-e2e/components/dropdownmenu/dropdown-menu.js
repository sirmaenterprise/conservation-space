'use strict';

let TestUtils = require('../../test-utils');

const SELECT_LOADER = '.loading';
const SELECT_MENU_DROPDOWN = '.dropdown-menu';
const SELECT_ITEM = '.menu-item';
const FIRST_SELECT_ITEM = SELECT_ITEM + ':first-of-type';

class DropdownMenu {
  constructor(element) {
    this.triggerMenuButton = element;
  }

  open() {
    this.getTriggerButton().click();
    browser.wait(EC.visibilityOf(this.triggerMenuButton.$(FIRST_SELECT_ITEM)), DEFAULT_TIMEOUT, 'Dropdown menu should be opened!');
    return this;
  }

  close() {
    this.getActionContainer().isDisplayed().then(isDisplayed => {
      if (isDisplayed) {
        this.getTriggerButton().click();
        this.isClosed();
      }
    });
  }

  isClosed() {
    browser.wait(EC.invisibilityOf(this.triggerMenuButton.$(FIRST_SELECT_ITEM)), DEFAULT_TIMEOUT, 'Dropdown menu should be closed!');
  }

  hasLoadingIndicator() {
    this.getTriggerButton().click();
    browser.wait(EC.presenceOf($(SELECT_LOADER)), DEFAULT_TIMEOUT, 'Loading indicator in dropdown menu should be present!');
    return this;
  }

  getTriggerButton() {
    return this.triggerMenuButton;
  }

  getActions() {
    return this.getActionContainer().all(by.xpath('li'));
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

  /**
   * Returns a promise which resolves with the label of the given action
   *
   * @param action the action which label is to be extracted
   */
  static getActionLabel(action) {
    return action.$('a').getText();
  }

  /**
   * Returns a promise which resolves with a flag true if the action
   * has a sub menu or false otherwise
   *
   * @param action the action which is to be tested
   */
  static hasSubMenu(action) {
    return action.$('.dropdown-submenu > .dropdown-menu').isPresent();
  }

  /**
   * Selects a given action by clicking on it. When no existing action
   * is specified the method will timeout. If the action is in a sub-menu,
   * then subMenu param should be used, otherwise the action will
   * not be executed.
   *
   * @param action the action to be selected
   * @param subMenu the sub-menu will open if specified
   */
  static selectAction(action, subMenu) {
    if (subMenu) {
      browser.actions().mouseMove(subMenu).perform();
    }
    browser.wait(EC.visibilityOf(action), DEFAULT_TIMEOUT, 'Action should be visible in dropdown menu!');
    action.click();
  }
}

module.exports.DropdownMenu = DropdownMenu;