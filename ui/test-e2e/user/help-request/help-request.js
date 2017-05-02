"use strict";

var SandboxPage = require('../../page-object').SandboxPage;
var Dialog = require('../../components/dialog/dialog');
var SingleSelectMenu = require('../../form-builder/form-control').SingleSelectMenu;

var SANDBOX_URL = '/sandbox/user/help-request/';

const SUBJECT = '#subject';
const TYPE = '#type';
const DESCRIPTION = '#description';

class HelpRequestSandbox extends SandboxPage {

  open() {
    super.open(SANDBOX_URL);
    var button = $('.container .btn');
    browser.wait(EC.visibilityOf(button), DEFAULT_TIMEOUT);
  }

  openDialog() {
    $('.btn').click();
    var dialog = new HelpRequestDialog($('.help-request'));
    return dialog;
  }
}

class HelpRequestDialog extends Dialog {
  constructor(element) {
    super(element);
    this.subject = element.$('input' + SUBJECT);
    browser.wait(EC.presenceOf(this.subject), DEFAULT_TIMEOUT);
    this.sendButton = element.$('.seip-btn-ok');
    this.dropdown = new SingleSelectMenu(this.element.$(TYPE));
    this.editor = this.getEditorElement();
  }

  setSubject(data) {
    this.subject.click();
    this.subject.sendKeys(data);
  }

  clearSubject() {
    this.clear(this.subject);
  }

  clear(element) {
    element.click();
    element.clear();
  }

  isSendButtonEnable() {
    return this.sendButton.isEnabled();
  }

  selectOption(option) {
    this.dropdown.selectOption(option);
  }

  clearOption() {
    this.dropdown.clearField(null);
  }

  addContentToEditor(value) {
    this.editor.click();
    this.editor.sendKeys(value);
  }
  getEditorElement() {
    let editor = this.element.$('#cke_help-request-editor .cke_wysiwyg_div');
    browser.wait(EC.elementToBeClickable(editor), DEFAULT_TIMEOUT);
    return editor;
  }

  clearEditor() {
    this.clear(this.editor);
    //after clear empty paragraf is left so we set key BACK_SPACE
    this.addContentToEditor("\uE003");
  }

  sendRequest() {
    this.ok();
    return false;
  }
}
module.exports.HelpRequestSandbox = HelpRequestSandbox;