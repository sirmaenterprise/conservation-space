'use strict';

let SandboxPage = require('../../../page-object').SandboxPage;
let FormWrapper = require('../../../form-builder/form-wrapper').FormWrapper;
let Notification = require('../../../components/notification').Notification;

class TransitionActionSandboxPage extends SandboxPage {

  open() {
    browser.get('/sandbox/idoc/actions/transition-action');
  }

  approve() {
    element(by.id('approve')).click();
    return new StateTransitionDialog($('#invalidObjectsList'));
  }

  reject() {
    element(by.id('reject')).click();
    return new Notification();
  }

  restart() {
    element(by.id('restart')).click();
    return new StateTransitionDialog($('#invalidObjectsList'));
  }

}

class StateTransitionDialog {

  constructor(element) {
    this.dialog = element;
    browser.wait(EC.visibilityOf(this.dialog), DEFAULT_TIMEOUT);
  }

  getForm() {
    let formWrapper = new FormWrapper(this.dialog.$('.panel-body'));
    formWrapper.waitUntilVisible();
    return formWrapper;
  }

  accept(expectedNotification) {
    $('.seip-btn-save').click();
    if(expectedNotification) {
      return new Notification();
    }
  }
}

module.exports = {
  TransitionActionSandboxPage,
  StateTransitionDialog
};