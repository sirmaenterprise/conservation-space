'use strict';

class ActionsMenu {

  constructor(element) {
    this.element = element.$('.actions-menu');
    browser.wait(EC.presenceOf(this.element), DEFAULT_TIMEOUT);
  }

  openMenu() {
    this.element.click();
  }

  executeAction(css) {
    this.openMenu();
    this.element.$(css).click();
  }

  editIdoc() {
    this.openMenu();
    this.element.$('.editDetails').click();
  }

  isDisplayed() {
    return this.element.isDisplayed();
  }

  isPresent() {
    return this.element.isPresent();
  }

}

module.exports = ActionsMenu;
