'use strict';

let SandboxPage = require('../page-object').SandboxPage;
let SingleSelectMenu = require('../form-builder/form-control').SingleSelectMenu;
let InputField = require('../form-builder/form-control').InputField;
let FormWrapper = require('../form-builder/form-wrapper').FormWrapper;
let ContextSelector = require('../components/contextselector/context-selector');

const INSTANCE_CREATE_PANEL_PAGE_URL = '/sandbox/create/instance-create-panel';

class InstanceCreatePanelSandboxPage extends SandboxPage {

  open() {
    browser.get(INSTANCE_CREATE_PANEL_PAGE_URL);
  }

  openCreateDialog() {
    element(by.className('show-create-dialog-btn')).click();
  }

  getInstanceCreatePanel() {
    // For some dumb reason there are two nested instances of instance-create-panel in the create dialog
    // so the element.by returns both and defaults to the first one which is also reported in test result.
    // So changing it to multi-selection and getting the first result gives the same result without the
    // annoying logging in the test report.
    return new InstanceCreatePanel($$('.instance-create-panel').first());
  }
}

class InstanceCreatePanel {

  constructor(element) {
    this.panel = element;
    browser.wait(EC.visibilityOf(this.panel), DEFAULT_TIMEOUT, 'Instance create panel should be visible!');
  }

  getTypesDropdown() {
    return new SingleSelectMenu(this.panel.$('.types'));
  }

  getSubTypesDropdown() {
    return new SingleSelectMenu(this.panel.$('.sub-types'));
  }

  selectSubType(subtype) {
    let subtypesDropdown = this.getSubTypesDropdown();
    subtypesDropdown.selectOption(subtype);
  }

  getTemplateSelector() {
    let element = this.panel.$('.idoc-template-selector');
    browser.wait(EC.visibilityOf(element), DEFAULT_TIMEOUT, 'Template selector menu should be visible!');
    return new SingleSelectMenu(element);
  }

  getForm() {
    let formWrapper = new FormWrapper($('.form-container'));
    formWrapper.waitUntilVisible();
    return formWrapper;
  }

  getErrorMessge() {
    return this.panel.element(by.className('no-definition-found-error'));
  }

  getCreateButton() {
    let createBtn = this.panel.$('.seip-create-btn');
    browser.wait(EC.visibilityOf(createBtn), DEFAULT_TIMEOUT);
    return createBtn;
  }

  isCreateButtonDisplayed() {
    return this.getCreateButton().isDisplayed();
  }

  isCreateButtonDisabled() {
    return this.getCreateButton().getAttribute('disabled');
  }

  createInstance(createAnother) {
    this.waitForTemplateSelector();
    return this.getCreateButton().click().then(() => {
      // if checkbox is checked then wait for created object header to appear
      if (createAnother) {
        return this.waitForCreatedObjectHeader();
      }
      return;
    });
  }

  waitForCreatedObjectHeader() {
    this.waitForTemplateSelector();
    browser.wait(EC.visibilityOf(this.panel.$('.created-object-header')), DEFAULT_TIMEOUT);
    return this.panel.$('.created-object-header');
  }

  selectCreateAnotherInstance() {
    this.waitForTemplateSelector();
    this.getCreateCheckbox().click();
  }

  getCreateCheckbox() {
    browser.wait(EC.visibilityOf(this.panel.$('.create-checkbox')), DEFAULT_TIMEOUT);
    browser.wait(EC.elementToBeClickable(this.panel.$('.create-checkbox')), DEFAULT_TIMEOUT);
    return this.panel.$('.create-checkbox');
  }

  getCloseButton() {
    return this.panel.$('.seip-close-btn');
  }

  fillDescription(text) {
    let descriptionField = this.getForm().getInputText('description');
    descriptionField.setValue(null, text);
  }

  getField(name) {
    browser.wait(EC.visibilityOf(this.panel.$('#' + name)), DEFAULT_TIMEOUT);
    return new InputField(this.panel.$('#' + name));
  }

  /**
   * Choose context item as it appears in the search results starting from 1
   * @param objectNumber
   */
  chooseContext(objectNumber) {
    let contextSelect = this.getContextSelector();
    contextSelect.clickSelectButton();
    browser.wait(EC.visibilityOf(element(by.css('.search-results'))), DEFAULT_TIMEOUT);
    contextSelect.selectContextElement(objectNumber - 1);
  }

  getContextSelector() {
    browser.wait(EC.visibilityOf(this.panel.$('.context-selector')), DEFAULT_TIMEOUT);
    return new ContextSelector(this.panel.$('.context-selector'));
  }

  /**
   * The models service is stubbed to bring an error message
   * when object 6 is chosen from the context selector.
   */
  chooseInvalidContext() {
    this.chooseContext(6);
  }

  toggleShowMore() {
    this.panel.$('.show-more').click();
  }

  waitForTemplateSelector() {
    browser.wait(EC.visibilityOf(this.panel.$('.idoc-template-selector')), DEFAULT_TIMEOUT);
  }

}

module.exports = {
  InstanceCreatePanel,
  InstanceCreatePanelSandboxPage
};
