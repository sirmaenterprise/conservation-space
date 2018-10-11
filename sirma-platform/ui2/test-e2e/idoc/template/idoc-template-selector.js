"use strict";

var SingleSelectMenu = require('../../form-builder/form-control').SingleSelectMenu;
var SandboxPage = require('../../page-object').SandboxPage;

const SANDBOX_URL = '/sandbox/idoc/template/idoc-template-selector/';

class IdocTemplateSelectorSandboxPage extends SandboxPage {

  open() {
    super.open(SANDBOX_URL);
    return this;
  }

  getTemplateSelector() {
    var element = new SingleSelectMenu($('.idoc-template-selector'));
    browser.wait(EC.visibilityOf($('.idoc-template-selector')), DEFAULT_TIMEOUT);
    return element;
  }

  getActiveCheckbox() {
    var element = $('.active-checkbox');
    browser.wait(EC.visibilityOf(element), DEFAULT_TIMEOUT);
    return element;
  }

  getSelectedTemplate() {
    return $('.selected-template');
  }

}

module.exports.IdocTemplateSelectorSandboxPage = IdocTemplateSelectorSandboxPage;

