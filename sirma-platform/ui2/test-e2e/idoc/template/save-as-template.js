"use strict";

var Dialog = require('../../components/dialog/dialog');
var SingleSelectMenu = require('../../form-builder/form-control').SingleSelectMenu;
var Notification = require('../../components/notification').Notification;
var SandboxPage = require('../../page-object').SandboxPage;

const SANDBOX_URL = '/sandbox/idoc/template/save-as-template/';

class SaveAsTemplateSandboxPage extends SandboxPage {

  open() {
    super.open(SANDBOX_URL);
    var button = $('.container .btn');
    browser.wait(EC.visibilityOf(button), DEFAULT_TIMEOUT);
    return this;
  }

  openDialog() {
    $('.btn').click();
    var dialog = new SaveAsTemplateDialog($('.modal-dialog'));
    dialog.waitUntilOpened();
    return dialog;
  }

  getNotification() {
    return new Notification().waitUntilOpened();
  }

}
module.exports.SaveAsTemplateSandboxPage = SaveAsTemplateSandboxPage;

class SaveAsTemplateDialog extends Dialog {

  constructor(element) {
    super(element);
  }

  enterTitle(title) {
    var titleField = this.getTitle();
    titleField.click();
    // fixes an issue causing the title not to be input correctly
    titleField.sendKeys(protractor.Key.NULL);
    titleField.sendKeys(title);
  }

  getTitleMessage() {
    return this.element.$('.template-title-message');
  }

  togglePrimary() {
    this.primaryButton.click();
  }

  getObjectType() {
    return new SingleSelectMenu(this.element.$('.template-select'));
  }

  getTitle() {
    return this.element.$('.template-title');
  }

  getPimaryButton() {
    return this.element.$('.template-primary i');
  }

  selectPurpose(number) {
    return this.element.$(by.name('purpose'))[number].click();
  }

  waitForDialog() {
    var dialog = new SaveAsTemplateDialog($('.modal-dialog'));
    dialog.waitUntilOpened();
  }

  selectOk() {
    var okButton = $('.seip-btn-ok');
    browser.wait(EC.elementToBeClickable(okButton), DEFAULT_TIMEOUT);
    okButton.click();
  }

}
module.exports.SaveAsTemplateDialog = SaveAsTemplateDialog;