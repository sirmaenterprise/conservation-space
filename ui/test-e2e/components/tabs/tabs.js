"use strict";

const REPEATER = {
  TABS: 'tab in tabs.config.tabs'
};

class Tabs {
  constructor() {
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
    return element.all(by.repeater(REPEATER.TABS));
  }

}

class Tab {
  constructor(element) {
    this.element = element;
  }

  getClasses() {
    return this.element.getAttribute('class');
  }

  click() {
    this.element.$('a').click();
  }

  getPostfix() {
    return this.element.$('.postfix');
  }
}

module.exports = Tabs;