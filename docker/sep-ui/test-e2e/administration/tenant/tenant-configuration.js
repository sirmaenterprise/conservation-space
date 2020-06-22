'use strict';

var FormWrapper = require('../../form-builder/form-wrapper').FormWrapper;

const LABEL = 'label';
const TOOLTIP = 'seip-hint';
const SAVE_BUTTON = '.save-button';
const CANCEL_BUTTON = '.cancel-button';
const FILTER_BUTTON = '.filter-button';
const TENANT_CONFIGURATION = '.tenant-configuration';

class TenantConfiguration {
  constructor() {
    this.element = $(TENANT_CONFIGURATION);
    this.wrapper = new FormWrapper(this.element);
    this.waitUntilVisible();
  }

  getSpecificElement(selector) {
    return this.element.$(selector);
  }

  getFilterButton() {
    return this.getSpecificElement(FILTER_BUTTON);
  }

  getSaveButton() {
    return this.getSpecificElement(SAVE_BUTTON);
  }

  isSaveButtonDisabled() {
    return this.getSaveButton().getAttribute('disabled').then((attribute) => {
      return !!attribute;
    });
  }

  getCancelButton() {
    return this.getSpecificElement(CANCEL_BUTTON);
  }

  isCancelButtonDisabled() {
    return this.getCancelButton().getAttribute('disabled').then((attribute) => {
      return !!attribute;
    });
  }

  getAllLabelFields() {
    return this.element.all(by.tagName(LABEL));
  }

  getAllTooltipFields() {
    return this.element.all(by.className(TOOLTIP));
  }

  modifyFieldValue(selector, value) {
    let element = this.getSpecificElement(selector);
    element.sendKeys(value);
    browser.wait(EC.textToBePresentInElementValue(element, value), DEFAULT_TIMEOUT);
  }

  getFieldValue(selector) {
    return this.getSpecificElement(selector).getAttribute('value');
  }

  getFieldType(selector){
    return this.getSpecificElement(selector).getAttribute('type');
  }

  isFieldDisplayed(selector) {
    return this.getSpecificElement(selector).isDisplayed();
  }

  isFieldPresent(selector) {
    return this.getSpecificElement(selector).isPresent();
  }

  waitUntilVisible() {
    //wait for both the form wrapper's content
    //and the tenant configuration form to show
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
    this.wrapper.waitUntilVisible();
  }

  expandAllRegions() {
    //select only region's heading not the entire region
    var regions = this.element.all(by.className('panel-heading'));
    regions.each((region) => {
      region.isDisplayed().then((visible) => {
        if(visible) {
          region.click();
        }
      });
    });
  }
}

module.exports = {
  TenantConfiguration
};