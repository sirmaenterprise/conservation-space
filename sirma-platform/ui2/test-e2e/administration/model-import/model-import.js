'use strict';

let SandboxPage = require('../../page-object').SandboxPage;
let PageObject = require('../../page-object').PageObject;
let Notification = require('../../components/notification').Notification;
let Dialog = require('../../components/dialog/dialog');
let CheckboxField = require('../../form-builder/form-control').CheckboxField;
let SingleSelectMenu = require('../../form-builder/form-control').SingleSelectMenu;
let path = require('path');

class ModelImportSandboxPage extends SandboxPage {
  open(failing, hasErrors) {
    let hash = '';

    hash += 'fail=' + (failing ? 'true' : 'false');
    hash += '&hasErrors=' + (hasErrors ? 'true' : 'false');

    super.open('sandbox/administration/model-import', hash);
    return this;
  }

  getModelImportPanel() {
    return new ModelImportPanel($('.model-import-panel'));
  }

  getDownloadedFileName() {
    return $('.file-saver').getText();
  }

  getDownloadRequest() {
    return browser.executeScript('return window.modelsServiceStub.download');
  }
}

class ModelImportPanel extends PageObject {

  constructor(element) {
    super(element);

    this.inputElement = element.$('.file-input');
    this.clearButton = element.$('.clear-button');
    this.importButton = element.$('.import-button');
  }

  selectFiles(...files) {
    // some browsers require the input to be visible
    browser.executeScript('$(".file-input").show()');

    files.forEach(file => this.inputElement.sendKeys(path.resolve(__dirname, file)));
  }

  clear() {
    this.clearButton.click();
  }

  isClearAllowed() {
    return this.clearButton.isDisplayed();
  }

  import() {
    this.importButton.click();
  }

  isImportAllowed() {
    return this.importButton.isDisplayed();
  }

  isImportTypeSelectionDisplayed() {
    return this.importTypeElement.isDisplayed();
  }

  selectDefinitionImportType() {
    this.getImportTypeDropdown().selectFromMenu(null, 1, true);
  }

  selectOntologyImportType() {
    this.getImportTypeDropdown().selectFromMenu(null, 2, true);
  }

  getSelectedImportType() {
    return this.getImportTypeDropdown().getSelectedValue();
  }

  getSelectedFiles() {
    let files = this.element.$$('.selected-files .selected-file');
    return files.map(element => element.getText());
  }

  waitForNotification() {
    return new Notification().waitUntilOpened();
  }

  getErrorsDialog() {
    let dialog = new Dialog($('.modal-dialog'));
    dialog.waitUntilOpened();

    return dialog;
  }

  getModelDownloadPanel() {
    return new ModelDownloadPanel(this.modelDownloadElement);
  }

  getImportTypeDropdown() {
    return new SingleSelectMenu(this.importTypeElement);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf($('.model-upload')), DEFAULT_TIMEOUT);
  }

  getDownloadOntologiesButton() {
    return new DownloadOntologyButton(this.downloadOntologiesElement);
  }

  get importTypeElement() {
    return this.element.$('.import-types');
  }

  get modelDownloadElement() {
    return this.element.$('.model-download');
  }

  get downloadOntologiesElement() {
    return this.element.$('.download-ontology');
  }
}

class ModelDownloadPanel {

  constructor(element) {
    this.element = element;
    browser.wait(EC.visibilityOf(element), DEFAULT_TIMEOUT);

    this.downloadButton = element.$('.download-button');
    this.selectAllTemplatesCheckbox = new CheckboxField(this.element.$('.select-all-templates'));
    this.selectAllDefinitionsCheckbox = new CheckboxField(this.element.$('.select-all-definitions'));
  }

  isDownloadAllowed() {
    return this.downloadButton.isEnabled();
  }

  getImportedTemplates() {
    return this.element.$$('.template-download-table tbody tr').then(elements => {
      return elements.map(element => new TemplateRow(element));
    });
  }

  selectAllTemplates() {
    this.selectAllTemplatesCheckbox.toggleCheckbox();
  }

  isSelectAllTemplatesCheckBoxSelected() {
    // because CheckboxItem.isChecked() returns string
    return this.selectAllTemplatesCheckbox.isChecked().then(result => result === 'true');
  }

  getImportedDefinitions() {
    return this.element.$$('.definition-download-table tbody tr').then(elements => {
      return elements.map(element => new DefinitionRow(element));
    });
  }

  selectAllDefinitions() {
    this.selectAllDefinitionsCheckbox.toggleCheckbox();
  }

  isSelectAllDefinitionsCheckBoxSelected() {
    // because CheckboxItem.isChecked() returns string
    return this.selectAllDefinitionsCheckbox.isChecked().then(result => result === 'true');
  }

  download() {
    this.downloadButton.click();
  }

  filter(term) {
    this.element.$('.filter-field').clear().sendKeys(term);
  }
}

class DownloadOntologyButton {

  constructor(element) {
    this.element = element;
    browser.wait(EC.visibilityOf(element), DEFAULT_TIMEOUT);
  }

  isDownloadAllowed() {
    return this.element.isEnabled();
  }

  download() {
    this.element.click();
  }
}

class DefinitionRow {

  constructor(element) {
    this.element = element;
    this.checkbox = new CheckboxField(element.$('td:nth-child(1)'));
  }

  isSelected() {
    // because CheckboxItem.isChecked() returns string
    return this.checkbox.isChecked().then(result => result === 'true');
  }

  click() {
    this.checkbox.toggleCheckbox();
  }

  getIdentifier() {
    return this.element.$('td:nth-child(2)').getText();
  }

  getType() {
    return this.element.$('td:nth-child(3)').getText();
  }

  isAbstract() {
    return this.element.$('td:nth-child(4)').getText();
  }

  getFileName() {
    return this.element.$('td:nth-child(5)').getText();
  }

  getModifiedOn() {
    return this.element.$('td:nth-child(6)').getText();
  }

  getModifiedBy() {
    return this.element.$('td:nth-child(7)').getText();
  }
}

class TemplateRow {

  constructor(element) {
    this.element = element;
    this.checkbox = new CheckboxField(element.$('td:nth-child(1)'));
    this.title = element.$('td:nth-child(2)').getText();
    this.purpose = element.$('td:nth-child(3)').getText();
    this.primary = element.$('td:nth-child(4)').getText();
    this.forType = element.$('td:nth-child(5)').getText();
    this.modifiedOn = element.$('td:nth-child(6)').getText();
    this.modifiedBy = element.$('td:nth-child(7)').getText();
  }

  getTitle() {
    return this.title;
  }

  getPurpose() {
    return this.purpose;
  }

  getPrimary() {
    return this.primary;
  }

  getForType() {
    return this.forType;
  }

  getModifiedOn() {
    return this.modifiedOn;
  }

  getModifiedBy() {
    return this.modifiedBy;
  }

  isSelected() {
    // because CheckboxItem.isChecked() returns string
    return this.checkbox.isChecked().then(result => result === 'true');
  }

  click() {
    this.checkbox.toggleCheckbox();
  }
}

module.exports.ModelImportSandboxPage = ModelImportSandboxPage;
module.exports.ModelImportPanel = ModelImportPanel;