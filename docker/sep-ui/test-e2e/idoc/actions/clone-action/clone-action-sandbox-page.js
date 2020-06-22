'use strict';

let SandboxPage = require('../../../page-object').SandboxPage;
let InstanceCreatePanelSandboxPage = require('../../../create/instance-create-panel').InstanceCreatePanelSandboxPage;

class CloneActionSandboxPage extends SandboxPage {

  open() {
    browser.get('/sandbox/idoc/actions/clone-action');
  }

  getCloneDialog(buttonId) {
    let button = element(by.id(buttonId));
    browser.wait(EC.elementToBeClickable(button), DEFAULT_TIMEOUT);
    button.click();
    return new CloneActionDialog($('.modal-content'));
  }
}

class CloneActionDialog {

  constructor(element) {
    this.dialog = element;
    browser.wait(EC.visibilityOf(this.dialog), DEFAULT_TIMEOUT);
    this.instanceCreatePanel = new InstanceCreatePanelSandboxPage().getInstanceCreatePanel();
    this.contextSelector = this.instanceCreatePanel.getContextSelector();
  }

  hasError() {
    return this.contextSelector.hasError();
  }

  getErrorMessage() {
    return this.contextSelector.getErrorMessage();
  }

  isSelectContextButtonEnabled() {
    return this.contextSelector.getSelectButton().isEnabled();
  }

  isClearContextButtonEnabled() {
    return this.contextSelector.getClearContextButton().isEnabled();
  }

  isClearContextButtonVisible() {
    return this.contextSelector.getClearContextButton() === null;
  }

  clearContext() {
    this.contextSelector.clickClearContextButton();
  }

  fillDescription(text) {
    this.instanceCreatePanel.fillDescription(text);
  }

  isCloneButtonDisabled() {
    return this.instanceCreatePanel.isCreateButtonDisabled().then((valued) => {
      return valued !== null;
    });
  }

  chooseContext() {
    this.instanceCreatePanel.chooseContext(2);
  }

  clone() {
    return this.instanceCreatePanel.getCreateButton().click();
  }

  getClonedInstanceLink() {
    return this.dialog.$('.created-object-header a');
  }
}

module.exports = {
  CloneActionSandboxPage,
  CloneActionDialog
};