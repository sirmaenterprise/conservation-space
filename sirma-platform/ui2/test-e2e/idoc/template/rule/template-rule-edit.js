"use strict";
var Dialog = require('../../../components/dialog/dialog');
var Notification = require('../../../components/notification').Notification;
var FormWrapper = require('../../../form-builder/form-wrapper').FormWrapper;
var Notification = require('../../../components/notification').Notification;
var SandboxPage = require('../../../page-object').SandboxPage;

const SANDBOX_URL = '/sandbox/idoc/template/template-rule-edit';

class TemplateRuleEditSandboxPage extends SandboxPage {

  open() {
    super.open(SANDBOX_URL);
    var button = $('.container .btn');
    browser.wait(EC.visibilityOf(button), DEFAULT_TIMEOUT);
    return this;
  }

  openForNewRule() {
    $('#edit_new_rule').click();
    var dialog = new TemplateRuleEditDialog($('.modal-dialog'));
    dialog.waitUntilOpened();
    return dialog;
  }

  openForExistingRule() {
    $('#edit_existing_rule').click();
    var dialog = new TemplateRuleEditDialog($('.modal-dialog'));
    dialog.waitUntilOpened();
    return dialog;
  }

  openForDefinitionWithNoEligibleFields() {
    $('#no_eligible_fields_definition').click();
    var dialog = new TemplateRuleEditDialog($('.modal-dialog'));
    dialog.waitUntilOpened();
    return dialog;
  }

  openForPrimaryTemplateWithSecondaryTemplates() {
    $('#edit_existing_rule_primary_secondary').click();
    var dialog = new TemplateRuleEditDialog($('.modal-dialog'));
    dialog.waitUntilOpened();
    return dialog;
  }

  getNotification() {
    return new Notification().waitUntilOpened();
  }

}

class TemplateRuleEditDialog extends Dialog {

  constructor(element) {
    super(element);
  }

  getForm() {
    var formWrapper = new FormWrapper(this.element.$('.template-rule-editor'));
    formWrapper.waitUntilVisible();
    return formWrapper;
  }

  getNotification() {
    return new Notification().waitUntilOpened();
  }

  getMessage() {
    return this.element.$('.message');
  }

}

module.exports.TemplateRuleEditSandboxPage = TemplateRuleEditSandboxPage;