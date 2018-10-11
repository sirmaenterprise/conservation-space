'use strict';

var Search = require('../../../search/components/search.js').Search;

class ObjectSelector {

  constructor() {
    this._element = $('.object-selector');
    this.waitUntilOpened();
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this._element), DEFAULT_TIMEOUT);
  }

  selectObjectSelectionMode(mode) {
    var selectObjectModeElement = this._element.$(`.inline-group input[name="selectObject"][value="${mode}"]`).element(by.xpath('..'));
    browser.wait(EC.visibilityOf(selectObjectModeElement), DEFAULT_TIMEOUT);
    selectObjectModeElement.click();
    return this;
  }

  clickIncludeCurrent() {
    let checkbox = this._element.$('.checkbox.include-current-object');
    browser.wait(EC.elementToBeClickable(checkbox), DEFAULT_TIMEOUT);
    checkbox.click();
    return this;
  }

  areObjectSelectorOptionsPresent() {
    return this._element.$('.object-selector > .inline-group').isPresent();
  }

  getSelectObjectMode() {
    return this._element.$('.inline-group input[name="selectObject"]:checked').getAttribute('value');
  }

  isSearchPresent() {
    return this._element.$(Search.COMPONENT_SELECTOR).isPresent();
  }

  isCurrentObjectHeaderPresent() {
    return this._element.$('.current-object-header > .instance-header').isPresent();
  }

  getSearch() {
    if (!this.search) {
      this.search = new Search($(Search.COMPONENT_SELECTOR));
    }
    return this.search;
  }

  isObjectSelectionModePresent(mode) {
    return this._element.$(`.inline-group input[name="selectObject"][value="${mode}"]`).isPresent();
  }
}

ObjectSelector.CURRENT_OBJECT = 'current';
ObjectSelector.MANUALLY = 'manually';
ObjectSelector.AUTOMATICALLY = 'automatically';

module.exports.ObjectSelector = ObjectSelector;
