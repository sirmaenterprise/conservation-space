let Dialog = require('../components/dialog/dialog');
let InstanceCreatePanel = require('../create/instance-create-panel').InstanceCreatePanel;
let FileUploadPanel = require('../file-upload/file-upload').FileUploadPanel;

class NewDialog extends Dialog {

  constructor() {
    super($('.modal'));
  }

  waitUntilLoaded() {
    this.waitUntilOpened();
  }

  getCreatePanel() {
    return new InstanceCreatePanel($('.instance-create-panel'));
  }

  getCreatePanelWithExtensions() {
    return new InstanceCreatePanel($('.extension .instance-create-panel'));
  }

  getUploadPanel() {
    let uploadTab = $('li.file-upload-panel > a');
    uploadTab.click();
    browser.wait(EC.elementToBeClickable($('.select-files-button')), DEFAULT_TIMEOUT);
    browser.wait(EC.presenceOf($('.file-upload-field')), DEFAULT_TIMEOUT);
    return new FileUploadPanel($('.extension.file-upload-panel'));
  }
}

module.exports = NewDialog;
