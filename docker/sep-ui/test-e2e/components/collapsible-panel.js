'use strict';

let hasClass = require('../test-utils.js').hasClass;
let PageObject = require('../page-object').PageObject;

/**
 * Page object which wraps the bootstrap's panel component.
 */
class CollapsiblePanel extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getPanel() {
    return this.element.$('.panel-default');
  }

  getPanelHeading() {
    return this.element.$('.panel-heading');
  }

  getPanelTitle() {
    return this.getPanelHeading().$('.panel-title');
  }

  getPanelBody() {
    return this.element.$('.panel-body');
  }

  isPanelExpanded() {
    return hasClass(this.getPanel(), 'expanded');
  }

  expand() {
    this.isPanelExpanded().then(expanded => {
      if (!expanded) {
        this.getPanelHeading().click();
        browser.wait(EC.visibilityOf(this.getPanelBody()), DEFAULT_TIMEOUT, 'Panel should be expanded and visible!');
      }
    });
  }

}

module.exports = {
  CollapsiblePanel
};
