'use strict';

var SandboxPage = require('../page-object').SandboxPage;
var SingleSelectMenu = require('../form-builder/form-control').SingleSelectMenu;
var InputField = require('../form-builder/form-control').InputField;

const INSTANCE_CREATE_PANEL_PAGE_URL = '/sandbox/create/instance-create-panel';

class InstanceCreatePanelSandboxPage extends SandboxPage {

  open() {
    browser.get(INSTANCE_CREATE_PANEL_PAGE_URL);
  }

  openCreateDialog() {
    element(by.className('show-create-dialog-btn')).click();
  }

  getInstanceCreatePanel() {
    return new InstanceCreatePanel($('.instance-create-panel'));
  }
}

class InstanceCreatePanel {

  constructor(element) {
    this.panel = element;
    browser.wait(EC.visibilityOf(this.panel), DEFAULT_TIMEOUT);
  }

  getTypesDropdown() {
    return new SingleSelectMenu(this.panel.$('.types'));
  }

  getSubTypesDropdown() {
    return new SingleSelectMenu(this.panel.$('.sub-types'));
  }

  getErrorMessge() {
    return this.panel.element(by.className('no-definition-found-error'));
  }

  getCreateButton() {
    return this.panel.$('.seip-create-btn');
  }

  isCreateButtonDisplayed() {
    return this.getCreateButton().isDisplayed();
  }

  isCreateButtonDisabled() {
    return this.getCreateButton().getAttribute('disabled');
  }

  createInstance() {
    this.getCreateButton().click().then(() => {
      this.waitForCreatedObjectHeader();
    });
  }

  waitForCreatedObjectHeader() {
    browser.wait(EC.visibilityOf(this.panel.$('.created-object-header')), DEFAULT_TIMEOUT);
    return this.panel.$('.created-object-header');
  }

  selectCreateAnotherInstance() {
    this.getCreateCheckbox().click();
  }

  getCreateCheckbox() {
    browser.wait(EC.elementToBeClickable(this.panel.$('.create-checkbox')), DEFAULT_TIMEOUT);
    return this.panel.$('.create-checkbox');
  }

  getCloseButton() {
    return this.panel.$('.seip-close-btn');
  }

  fillDescription(text) {
    this.getField('description-wrapper').setValue(null, 'content');
  }

  getField(name) {
    browser.wait(EC.visibilityOf(this.panel.$('#' + name)), DEFAULT_TIMEOUT);
    return new InputField(this.panel.$('#' + name));
  }

}

module.exports = {
  InstanceCreatePanel,
  InstanceCreatePanelSandboxPage
};
