"use strict";

var ContextualHelp = require('../help/contextual-help').ContextualHelp;

class Dialog {

  constructor(element) {
    if (!element) {
      throw new Error('Dialog element must be provided to the constructor!');
    }
    this.element = element;
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  waitUntilClosed() {
    browser.wait(EC.stalenessOf(this.element), DEFAULT_TIMEOUT);
  }

  isPresent() {
    return this.element.isPresent();
  }

  getTitleText() {
    return this.getTitleElement().getText();
  }

  getTitleElement() {
    return this.element.$('.modal-title');
  }

  getHeaderElement() {
    return this.element.$('.modal-header');
  }

  getBodyElement() {
    return this.element.$('.modal-body');
  }

  getWarningMessageElement() {
    return this.element.$('.alert-message');
  }

  getWarningPopoverTitle() {
    return this.getWarningMessageElement().getAttribute('data-title');
  }

  getWarningPopoverBody() {
    return this.getWarningMessageElement().getAttribute('data-content');
  }

  getButtons() {
    return this.element.all(by.css('.modal .modal-footer .btn'));
  }

  closeModal() {
    this.element.$('.close').click();
    this.waitUntilClosed();
  }

  clickButton(selector) {
    return this.element.$('.modal-footer .btn' + selector).click();
  }

  ok() {
    this.getOkButton().click();
    this.waitUntilClosed();
  }

  save() {
    let saveButton = this.getSaveButton();
    browser.wait(EC.elementToBeClickable(saveButton), DEFAULT_TIMEOUT);
    saveButton.click();
  }

  getSaveButton() {
    return this.element.$('.seip-btn-save');
  }

  cancel() {
    this.getCancelButton().click();
    this.waitUntilClosed();
  }

  close() {
    this.getCloseButton().click();
    this.waitUntilClosed();
  }

  confirm() {
    this.getConfirmButton().click();
    this.waitUntilClosed();
  }

  getConfirmButton() {
    return this.element.$('.modal-footer .seip-btn-confirm');
  }

  cancelChanges() {
    this.getCancelChangesButton().click();
    this.waitUntilClosed();
  }

  getCancelChangesButton() {
    return this.element.$('.modal-footer .seip-btn-cancel');
  }

  getOkButton() {
    // don't look for .seip-btn-* because the suffix is dynamic and can differ according
    // to any specific dialog config
    return this.element.$('.modal-footer .btn-primary');
  }

  getCancelButton() {
    var cancelButton = this.element.$('.modal-footer .btn-default');
    browser.wait(EC.elementToBeClickable(cancelButton), DEFAULT_TIMEOUT);
    return cancelButton;
  }

  getCloseButton() {
    return this.element.$('.modal-footer .seip-btn-close');
  }

  getHelpElement() {
    var header = this.getHeaderElement();
    return header.$(ContextualHelp.COMPONENT_SELECTOR);
  }
}
Dialog.COMPONENT_SELECTOR = '.modal-dialog';

module.exports = Dialog;