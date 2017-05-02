"use strict";

// http://stackoverflow.com/questions/34215697/remote-file-upload-protractor-test/34367189
// https://github.com/angular/protractor/issues/1959
var remote = require('selenium-webdriver/remote');
var path = require('path');

var SandboxPage = require('../page-object').SandboxPage;
var FormWrapper = require('../form-builder/form-wrapper').FormWrapper;
var SingleSelectMenu = require('../form-builder/form-control').SingleSelectMenu;
var Button = require('../form-builder/form-control').Button;
var Notification = require('../components/notification').Notification;

const URL = '/sandbox/file-upload';

class FileUploadSandboxPage extends SandboxPage {
  open(failing, multiple, timeout, id) {
    var hash = '';
    if (failing) {
      hash += 'fail=true'
    } else {
      hash += 'fail=false'
    }
    if (multiple) {
      hash += '&multiple=true'
    }
    if (timeout) {
      hash += '&timeout=' + timeout
    }
    if (id) {
      hash += '&id=' + id;
    }

    super.open(URL, hash);
    return this;
  }

  getFileUpload() {
    return new FileUploadPanel($('.file-upload-panel'));
  }
}

class FileUploadPanel {
  constructor(element) {
    browser.setFileDetector(new remote.FileDetector());
    this.element = element;
    this.inputElement = element.$('.file-upload-field');
  }

  selectFile(filePath) {
    // some browsers require the input to be visible
    browser.executeScript('$(".file-upload-field").show()');

    this.inputElement.sendKeys(path.resolve(__dirname, filePath));
  }

  getEntry(index) {
    return new FileEntry(this.element.$('.file-entry:nth-child(' + index + ')'));
  }

  getUploadAllButton() {
    return new Button(this.element.$('.uploadall-btn'));
  }

  getNotification() {
    return new Notification().waitUntilOpened();
  }
}

class FileEntry {
  constructor(element) {
    this.element = element;
  }

  getFileName() {
    return this.element.$('.file-name').getText();
  }

  getUploadButton() {
    return new Button(this.element.$('.upload-button'));
  }

  getRemoveButton() {
    return new Button(this.element.$('.remove-button'));
  }

  isPresent() {
    return this.element.isPresent();
  }

  getType() {
    return new SingleSelectMenu(this.element.$('.types'));
  }

  getSubType() {
    return new SingleSelectMenu(this.element.$('.sub-types'));
  }

  getForm() {
    return new FormWrapper(this.element);
  }

  getProgressBar() {
    return new ProgressBar(this.element.$('.progress'));
  }

  getMessage() {
    return new Message(this.element.$('.message'));
  }

  getInstanceHeader() {
    return this.element.$('.instance-header');
  }

  waitForInstanceHeader() {
    browser.wait(EC.presenceOf(this.getInstanceHeader()), DEFAULT_TIMEOUT);
  }
}

class Message {
  constructor(element) {
    this.element = element;
  }

  waitUntilLoaded() {
    browser.wait(EC.presenceOf(this.element), DEFAULT_TIMEOUT);
  }

  waitForText() {
    this.waitUntilLoaded();

    browser.wait(() => {
        return this.element.getText().then(function (text) {
          return text.length > 0;
        });
      }, DEFAULT_TIMEOUT
    );
    return this;
  }

  getText() {
    return waitForText().getText();
  }

}

class ProgressBar {
  constructor(element) {
    this.element = element;
  }

  getValue() {
    return this.element.$('.progress-bar').getText();
  }
}

module.exports.FileUploadSandboxPage = FileUploadSandboxPage;