'use strict';
var SandboxPage = require('../../../page-object').SandboxPage;
let PageObject = require('../../../page-object').PageObject;
var TestUtils = require('../../../test-utils');

const ORDER_BY_SELECTOR = '.order-by';
const ORDER_DIRECTION_SELECTOR = '.sort-by';

const SANDBOX_URL = 'sandbox/search/components/common/order-toolbar';

class OrderToolbarSandbox extends SandboxPage {

  open() {
    super.open(SANDBOX_URL);
  }

  getOrderByValue() {
    return $('#toolbar-data #order-by').getText();
  }

  getOrderDirectionValue() {
    return $('#toolbar-data #order-direction').getText();
  }

  toggleToolbarState() {
    return $('#toolbar-controls #toggle-toolbar').click();
  }

  getOrderToolbar() {
    return new OrderToolbar($(OrderToolbar.COMPONENT_SELECTOR));
  }
}

/**
 * Page object for the order toolbar.
 *
 * @author Svetlozar Iliev
 */
class OrderToolbar extends PageObject {

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
    browser.wait(EC.visibilityOf(this.getOrderByElement()), DEFAULT_TIMEOUT);
    browser.wait(EC.visibilityOf(this.getOrderDirectionButton()), DEFAULT_TIMEOUT);
  }

  openOrderByDropdown() {
    return this.getOrderByDropdownButton().click();
  }

  getOrderByDropdown() {
    // toggle the drop down first
    this.openOrderByDropdown();
    // return all options present inside the drop down
    return this.element.all(by.css('.dropdown-menu > li'));
  }

  getOrderByDropdownOption(index) {
    return this.getOrderByDropdown().get(index);
  }

  selectOrderByOption(index) {
    return this.getOrderByDropdownOption(index).click();
  }

  toggleOrderDirection() {
    this.getOrderDirectionButton().click();
    return this;
  }

  getOrderDirection() {
    let directionIcon = this.getOrderDirectionButton().$('.fa');
    return TestUtils.hasClass(directionIcon, OrderToolbar.ASCENDING).then((hasClass) => {
      return (hasClass) ? OrderToolbar.ASCENDING : OrderToolbar.DESCENDING;
    });
  }

  getOrderByOption() {
    return this.element.$('.order-by-option').getText();
  }

  getOrderByOptions() {
    let options = this.getOrderByDropdown();
    var optionsPromises = options.map((option) => {
      return option.getText();
    });
    return Promise.all(optionsPromises).then((labels) => {
      return labels;
    });
  }

  getOrderByDropdownButton() {
    return this.element.$('.order-by-dropdown-button');
  }

  getOrderByElement() {
    return this.element.$(ORDER_BY_SELECTOR);
  }

  getOrderDirectionButton() {
    return this.element.$(ORDER_DIRECTION_SELECTOR);
  }

  isOptionDisabled(index) {
    var selectedOption = this.getOrderByDropdownOption(index);
    // options are list items each of which wraps a link tag
    var actualOption = selectedOption.element(by.tagName('a'));
    // assert that the wrapped link tag is the disabled one
    return TestUtils.hasClass(actualOption, 'order-disabled');
  }

  isOptionPresent(index) {
    return this.getOrderByDropdownOption(index).isPresent();
  }
}

OrderToolbar.ASCENDING = 'ascending';
OrderToolbar.DESCENDING = 'descending';
OrderToolbar.COMPONENT_SELECTOR = '.order-toolbar';

module.exports = {
  OrderToolbar,
  OrderToolbarSandbox
};