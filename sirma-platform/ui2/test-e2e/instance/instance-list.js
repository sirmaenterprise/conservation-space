'use strict';

var PageObject = require('../page-object').PageObject;
var SandboxPage = require('../page-object').SandboxPage;

const SANDBOX_URL = '/sandbox/instance/instance-list';

const LIST_ITEM_WRAPPER_SELECTOR = '.instance-list-item-wrapper';

class InstanceListSandboxPage extends SandboxPage {

  open() {
    super.open(SANDBOX_URL);
  }

  getNoSelectionList() {
    return new InstanceList($('.pure .instance-list'));
  }

  getSingleSelectionList() {
    return new InstanceList($('.single .instance-list'));
  }

  getMultipleSelectionList() {
    return new InstanceList($('.multi .instance-list'));
  }

  getExclusionsList() {
    return new InstanceList($('.exclusions .instance-list'));
  }

  getItemsInputValue() {
    return $('#items').getAttribute('value').then((value) => {
      return JSON.parse(value || '[]');
    });
  }
}

class InstanceList extends PageObject {

  constructor(element) {
    super(element)
  }

  getItemsCount() {
    return this.element.all(by.css(LIST_ITEM_WRAPPER_SELECTOR)).count();
  }

  getItems() {
    return this.element.all(by.css(LIST_ITEM_WRAPPER_SELECTOR)).then((elements) => {
      return elements.map((el) => new InstanceListItem(el));
    });
  }

  getSelectAllButton() {
    return this.element.$('.select-all');
  }

  getDeselectAllButton() {
    return this.element.$('.deselect-all');
  }

  waitUntilOpened() {
    browser.wait(EC.presenceOf(this.element), DEFAULT_TIMEOUT);
  }

  /**
   * Waits until at least one result is visible meaning that there are search results rendered.
   */
  waitForResults() {
    this.waitUntilOpened();
    browser.wait(() => {
      return this.getItemsCount().then((count) => {
        return count > 0;
      });
    }, DEFAULT_TIMEOUT);
  }
}

class InstanceListItem extends PageObject {
  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getSelectType() {
    return this.element.$('input').getAttribute('type').then((value) => {
      return value === 'radio' ? 'single' : 'multiple';
    });
  }

  isDisabled() {
    return this.element.$('label').getAttribute('class').then((value) => {
      return value.includes('state-disabled');
    });
  }

  select() {
    var label = this.element.$('label');
    browser.wait(EC.elementToBeClickable(label), DEFAULT_TIMEOUT);
    return label.click();
  }

  isSelected() {
    return this.element.$('input').getAttribute('checked').then((value) => {
      return !!value;
    });
  }
}

module.exports = {
  InstanceList,
  InstanceListItem,
  InstanceListSandboxPage
};