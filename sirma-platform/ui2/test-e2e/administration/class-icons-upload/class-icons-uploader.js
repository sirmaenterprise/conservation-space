"use strict";

var SandboxPage = require('../../page-object').SandboxPage;
var remote = require('selenium-webdriver/remote');
var path = require('path');
var Dialog = require('../../components/dialog/dialog');

const CORRECT_ICON = 'logo.png';
const WRONG_FILE = 'class-icons-uploader.js';
const SANDBOX_URL = '/sandbox/administration/class-icons-upload/';

class ClassIconsUploadSandboxPage extends SandboxPage {

  open() {
    super.open(SANDBOX_URL);
  }

  openDialog() {
    var button = $('.btn');
    browser.wait(EC.elementToBeClickable(button), DEFAULT_TIMEOUT);
    button.click();
    var dialog = new ClassIconsUploadDialog($('.modal-dialog'));
    dialog.waitUntilOpened();
    return dialog;
  }

}

class ClassIconsUploadDialog extends Dialog {

  constructor(element) {
    super(element);
  }

  getUploaders() {
    return this.element.$$('.icon-uploader');
  }

  getUploader(index) {
    return new IconUploader(this.getUploaders().get(index), index);
  }

}

class IconUploader {
  constructor(element, index) {
    browser.setFileDetector(new remote.FileDetector());
    this.element = element;
    this.index = index;
  }

  waitUntilPresent(element) {
    browser.wait(EC.visibilityOf(element), DEFAULT_TIMEOUT);
  }

  selectIcon(correct) {
    //driver.findElements is used because executeScript doesn't work with element(locator)
    browser.driver.findElements(by.css('input[type="file"]')).then((inputs)=> {
      browser.executeScript('$(arguments[0]).show()', inputs[this.index]);
      inputs[this.index].sendKeys(path.resolve(__dirname, correct ? CORRECT_ICON : WRONG_FILE));
    });
  }

  getErrorMessage() {
    let error = this.element.$('.icon-error');
    this.waitUntilPresent(error);
    return error;
  }

  getSelectedIcon() {
    let icon = this.element.$('img');
    this.waitUntilPresent(icon);
    return icon;
  }

  removeSelectedIcon() {
    let removeButton = this.element.$('.remove-icon-button');
    this.waitUntilPresent(removeButton);
    removeButton.click();
  }
}

module.exports = {
  ClassIconsUploadDialog,
  ClassIconsUploadSandboxPage,
  IconUploader
};