'use strict';
let Dialog = require('../../components/dialog/dialog');
let BasicSearchCriteria = require('../../search/components/common/basic-search-criteria.js').BasicSearchCriteria;
var SandboxPage = require('../../page-object').SandboxPage;

const SANDBOX_PATH = '/sandbox/idoc/dialogs/add-relation/';
const RELATION_PANEL = '.add-relation-panel';
const RELATION_BUTTON_SELECTOR = ".relations button";

class AddRelationSandboxPage extends SandboxPage {

  open() {
    super.open(SANDBOX_PATH);
    browser.wait(EC.visibilityOf($(RELATION_BUTTON_SELECTOR)), DEFAULT_TIMEOUT);
  }

  openDialog() {
    $(RELATION_BUTTON_SELECTOR).click();
    var dialog = new AddRelationDialog($('.modal-dialog'));
    dialog.waitUntilOpened();
    return dialog;
  }
}

class AddRelationDialog extends Dialog {
  constructor(element) {
    super(element);
  }

  getSearchCriteria(){
    return new BasicSearchCriteria($(RELATION_PANEL));
  }
}

module.exports = {
  AddRelationSandboxPage,
  AddRelationDialog
};