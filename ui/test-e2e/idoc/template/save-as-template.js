"use strict";

var Dialog = require('../../components/dialog/dialog');
var SingleSelectMenu = require('../../form-builder/form-control').SingleSelectMenu;
var Notification = require('../../components/notification').Notification;

const SANDBOX_URL = '/sandbox/idoc/template/save-as-template/';

class SaveAsTemplateSandboxPage {

  open() {
    browser.get(SANDBOX_URL);
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
    titleField.sendKeys(title);
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
}
module.exports.SaveAsTemplateDialog = SaveAsTemplateDialog;