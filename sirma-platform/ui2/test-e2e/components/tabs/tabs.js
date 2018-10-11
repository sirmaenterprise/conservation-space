'use strict';

let PageObject = require('../../page-object').PageObject;
let SandboxPage = require('../../page-object').SandboxPage;

const SANDBOX_URL = 'sandbox/components/tabs';

const REPEATER = {
  TABS: 'tab in tabs.config.tabs'
};

class TabsSandbox extends SandboxPage {

  open() {
    super.open(SANDBOX_URL);
    return this;
  }

  getHorizontalTabs() {
    return new Tabs($('#horizontal'));
  }

  getVerticalTabs() {
    return new Tabs($('#vertical'));
  }

  getSortedTabs() {
    return new Tabs($('#sorted'));
  }

  activateHorizontal() {
    return $('.activate-horizontal').click();
  }

  activateVertical() {
    return $('.activate-vertical').click();
  }

  activateSorted() {
    return $('.activate-sorted').click();
  }
}

class Tabs extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  /**
   * Returns tab that corresponds to the given index.
   *
   * @param id The index of the tab in the tab menu. The first tab has index 0.
   * @returns {Tab} The tab element
   */
  getTab(id) {
    return new Tab(element(by.repeater(REPEATER.TABS).row(id)));
  }

  getTabs() {
    return this.element.all(by.repeater(REPEATER.TABS));
  }

  isDisplayed() {
    return this.element.isDisplayed();
  }

  isPresent() {
    return this.element.isPresent();
  }
}

class Tab extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getClasses() {
    return this.element.getAttribute('class');
  }

  click() {
    return this.element.$('a').click();
  }

  getLabel() {
    return this.element.$('a').getText();
  }

  getPostfix() {
    return this.element.$('.postfix');
  }
}

module.exports = {
  TabsSandbox,
  Tabs,
  Tab
};