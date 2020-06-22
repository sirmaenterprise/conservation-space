"use strict";

var SandboxPage = require('../../page-object').SandboxPage;
var SANDBOX_URL = '/sandbox/user/help-request/';
var ContextSelector = require('../../components/contextselector/context-selector');
var SingleSelectMenu = require('../../form-builder/form-control').SingleSelectMenu;
var InputField = require('../../form-builder/form-control').InputField;

class HelpRequestSandbox extends SandboxPage {

  open() {
    super.open(SANDBOX_URL);
    var button = $('.container .btn');
    browser.wait(EC.visibilityOf(button), DEFAULT_TIMEOUT);
  }

  openDialog() {
    $('.btn').click();
    return new InstanceCreatePanel(element(by.className('instance-create-panel')));
  }

}

class InstanceCreatePanel {

  constructor(element) {
    this.element = element;
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getContextSelector() {
    return new ContextSelector(this.element.$('.context-selector'));
  }

  getTypesDropdown() {
    return new SingleSelectMenu(this.element.$('.types'));
  }

  create() {
    let createBtn = this.element.$('.seip-create-btn');
    browser.wait(EC.visibilityOf(createBtn), DEFAULT_TIMEOUT);
    createBtn.click();
  }

  setValue(name, text) {
    browser.wait(EC.visibilityOf(this.element.$('#' + name)), DEFAULT_TIMEOUT);
    new InputField(this.element.$('#' + name)).setValue(null, text);
  }

  waitUntillClosed() {
    browser.wait(EC.stalenessOf($('.seip-modal')), DEFAULT_TIMEOUT);
  }

}

module.exports.HelpRequestSandbox = HelpRequestSandbox;