'use strict';

let SandboxPage = require('../../../page-object').SandboxPage;
let Notification = require('../../../components/notification').Notification;
let Dialog = require('../../../components/dialog/dialog');
let InstanceCreatePanel = require('../../../create/instance-create-panel').InstanceCreatePanel;


class ChangeTypeActionSandboxPage extends SandboxPage {

  getChangeTypeDialog() {
    return new ChangeTypeDialog($('.change-type-modal-dialog'));
  }

}

class ChangeTypeDialog extends InstanceCreatePanel {

  constructor(element) {
    super(element);
  }

  getDialogTitle() {
    return this.panel.$('.modal-title').getText();
  }

  getCancelButton() {
    return this.panel.$('.seip-btn-cancel');
  }

  isCancelButtonClickable() {
    browser.wait(EC.elementToBeClickable(this.getCancelButton()), DEFAULT_TIMEOUT, 'Cancel button should be clickable!');
    return true;
  }

  getChangeTypeButton() {
    return this.panel.$('.seip-btn-save');
  }

  isChangeTypeButtonDisabled() {
    let btn = this.getChangeTypeButton();
    return btn.isEnabled().then(value => !value);
  }

  cancel() {
    this.getCancelButton().click();
    this.waitUntilClosed();
  }

  changeType() {
    browser.wait(EC.elementToBeClickable(this.getChangeTypeButton()), DEFAULT_TIMEOUT, 'Change type button should be clickable!');
    this.getChangeTypeButton().click();
    let confirmation = new ConfirmChangeTypeDialog($('.change-type-confirmation'));
    confirmation.waitUntilOpened();
    return confirmation;
  }

  waitUntilClosed() {
    browser.wait(EC.stalenessOf(this.panel), DEFAULT_TIMEOUT, 'Change type dialog should be closed!');
  }
}

class ConfirmChangeTypeDialog extends Dialog {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf($('.modal-header.confirmation')), DEFAULT_TIMEOUT);
  }

  cancel() {
    this.clickButton('.seip-btn-no');
    this.waitUntilClosed();
  }

  confirm() {
    this.clickButton('.seip-btn-yes');
    this.waitUntilClosed();
    let notification = new Notification();
    notification.waitUntilOpened();
    return notification;
  }
}

module.exports = {
  ChangeTypeActionSandboxPage,
  ChangeTypeDialog,
  ConfirmChangeTypeDialog
};