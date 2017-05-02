"use strict";

class ContextSelector {

  constructor(element) {
    if (!element) {
      throw new Error('Context selector element must be provided to the constructor!');
    }
    this.element = element;
  }

  getSelectButton() {
    return this.element.$('.open-picker-btn');
  }

  clickSelectButton() {
    this.getSelectButton().click();
  }

  getClearContextButton() {
    return this.element.$('.clear-context-btn');
  }

  clickClearContextButton() {
    this.getClearContextButton().click();
  }

  getContextPathElement() {
    return this.element.$('.context-path');
  }

  getContextPathText() {
    return this.getContextPathElement().getText();
  }

  getContextIdElement() {
    return element(by.id('selected-item'));
  }

  getContextIdText() {
    return this.getContextIdElement().getAttribute('value');
  }

}

module.exports = ContextSelector;