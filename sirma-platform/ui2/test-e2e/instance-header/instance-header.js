'use strict';

let SandboxPage = require('../page-object').SandboxPage;
let FormWrapper = require('./../form-builder/form-wrapper').FormWrapper;
let ObjectControl = require('../form-builder/form-control.js').ObjectControl;

const INSTANCE_HEADER_PAGE_URL = '/sandbox/instance-header';

class InstanceHeaderSandboxPage extends SandboxPage {

  open() {
    super.open(INSTANCE_HEADER_PAGE_URL);
  }

  getForm() {
    let formWrapper = new FormWrapper($('.form-container'));
    formWrapper.waitUntilVisible();
    return formWrapper;
  }

  changeTitle(title) {
    this.getForm().getInputText('title').setValue(null, title);
  }

  changeCountry(country) {
    this.getForm().getCodelistField('country').selectOption(country);
  }

  changeDueDate() {
    this.getForm().getDateField('dueDate').setDate(null, '25.11.17 00:00');
  }

  getReferencesField() {
    return this.getForm().getObjectControlField('references');
  }

  loadHeader() {
    return $('.load-header').click();
  }

  getIconHeader() {
    return new InstanceHeader($('#icon_header'));
  }

  getThumbnailHeader() {
    return new InstanceHeader($('#thumbnail_header'));
  }
}

class InstanceHeader {

  constructor(element) {
    this.element = element;
  }

  getHeader() {
    return this.element.$('.instance-data').getText();
  }

  getHeaderLinkText() {
    return this.element.$('.instance-link').getText();
  }

  getIconUrl() {
    return this.element.$('.instance-icon img').getAttribute('src');
  }

  getField(fieldName) {
    browser.wait(EC.presenceOf(this.element.$(`[data-property="${fieldName}"]`)), DEFAULT_TIMEOUT);
    return this.element.$(`[data-property="${fieldName}"]`);
  }

  getDueDate() {
    return this.getField('dueDate').getText();
  }

  getCountry() {
    return this.getField('country').getText();
  }

  getReferences() {
    return new ObjectControl(this.getField('references'));
  }

}

const INSTANCE_HEADERS = {
  HEADER_DEFAULT: 'default_header',
  HEADER_COMPACT: 'compact_header',
  HEADER_BREADCRUMB: 'breadcrumb_header',
  HEADER_TOOLTIP: 'tooltip_header'
};

module.exports = {InstanceHeaderSandboxPage, InstanceHeader, INSTANCE_HEADERS};