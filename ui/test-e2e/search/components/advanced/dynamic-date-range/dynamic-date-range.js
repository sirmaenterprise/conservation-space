'use strict';

var SingleSelectMenu = require('../../../../form-builder/form-control.js').SingleSelectMenu;
var SandboxPage = require('../../../../page-object').SandboxPage;

const SANDBOX_PAGE_URL = 'sandbox/search/components/advanced/dynamic-date-range/';

const DEFAULT_FORM = '#default_values .dynamic-date-range';
const PREDEFINED_FORM = '#predefined_values .dynamic-date-range';
const STATE_BUTTON = '#toggle_state';

const DATE_STEP = '.date-step';
const DATE_OFFSET = '.date-offset';
const DATE_OFFSET_TYPE = '.date-offset-type';

/**
 * Page object for the sandbox page bootstrapping the dynamic date range form component.
 *
 * @author Mihail Radkov
 */
class DynamicDateRangeSandboxPage extends SandboxPage {

  open() {
    super.open(SANDBOX_PAGE_URL);
    this.waitUntilOpened();
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf($('.dynamic-date-range-stub')), DEFAULT_TIMEOUT);
  }

  getDefaultForm() {
    var defaultForm = $(DEFAULT_FORM);
    browser.wait(EC.visibilityOf(defaultForm), DEFAULT_TIMEOUT);
    return new DynamicDateRange(defaultForm);
  }

  getPredefinedForm() {
    var predefinedForm = $(PREDEFINED_FORM);
    browser.wait(EC.visibilityOf(predefinedForm), DEFAULT_TIMEOUT);
    return new DynamicDateRange(predefinedForm);
  }

  toggleDefaultFormState() {
    var toggleButton = $(STATE_BUTTON);
    browser.wait(EC.visibilityOf(toggleButton), DEFAULT_TIMEOUT);
    return toggleButton.click();
  }
}

/**
 * Page object for working with the dynamic date range form and its components.
 *
 * @author Mihail Radkov
 */
class DynamicDateRange {

  constructor(element) {
    if (!element) {
      throw new Error('Cannot instantiate PO without element!');
    }
    this.element = element;
    this.waitUntilOpened();
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
    browser.wait(EC.visibilityOf(this.dateStepSelectElement), DEFAULT_TIMEOUT);
  }

  isDisplayed() {
    return this.dateStepSelectElement.isDisplayed();
  }

  isDisabled() {
    return Promise.all([
      this.dateStepSelectMenu.isDisabled(),
      this.dateOffsetInputElement.getAttribute('disabled'),
      this.dateOffsetTypeSelectMenu.isDisabled()
    ]).then((states) => {
      return states[0] && !!states[1] && states[2];
    });
  }

  selectDateStep(dateStep) {
    return this.dateStepSelectMenu.selectFromMenu(undefined, dateStep, false);
  }

  getSelectedDateStep() {
    return this.dateStepSelectMenu.getSelectedValue();
  }

  enterDateOffset(offset) {
    return this.dateOffsetInputElement.sendKeys(offset);
  }

  getDateOffsetValue() {
    return this.dateOffsetInputElement.getAttribute('value');
  }

  selectDateOffsetType(dateOffsetType) {
    return this.dateOffsetTypeSelectMenu.selectFromMenu(undefined, dateOffsetType, false);
  }

  getSelectedDateOffsetType() {
    return this.dateOffsetTypeSelectMenu.getSelectedValue();
  }

  get dateStepSelectElement() {
    return this.element.$(DATE_STEP);
  }

  get dateStepSelectMenu() {
    return new SingleSelectMenu(this.dateStepSelectElement);
  }

  get dateOffsetInputElement() {
    return this.element.$(DATE_OFFSET);
  }

  get dateOffsetTypeSelectElement() {
    return this.element.$(DATE_OFFSET_TYPE);
  }

  get dateOffsetTypeSelectMenu() {
    return new SingleSelectMenu(this.dateOffsetTypeSelectElement);
  }

}
DynamicDateRange.COMPONENT_SELECTOR = '.dynamic-date-range';

module.exports = {
  DynamicDateRangeSandboxPage,
  DynamicDateRange
};