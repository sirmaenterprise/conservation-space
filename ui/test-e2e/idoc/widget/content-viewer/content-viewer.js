'use strict';

var Widget = require('../widget').Widget;
var WidgetConfigDialog = require('../widget').WidgetConfigDialog;
var SandboxPage = require('../../../page-object').SandboxPage;

class ContentViewerSandboxPage extends SandboxPage {

  open() {
    super.open('/sandbox/idoc/widget/content-viewer/');
    this.insertButton = $('#insert_widget');
    this.waitUntilOpened();
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.insertButton), DEFAULT_TIMEOUT);
  }

  changeSearchDataset(dataset) {
    var dataSetOption = $('#' + dataset);
    browser.wait(EC.visibilityOf(dataSetOption), DEFAULT_TIMEOUT);
    dataSetOption.click();
  }

  insertWidget() {
    this.insertButton.click();
  }

  getWidget() {
    return new ContentViewer($('.widget[widget="content-viewer"]'));
  }
}

ContentViewerSandboxPage.EMPTY_DATASET = 'empty';
ContentViewerSandboxPage.SINGLE_DATASET = 'single';
ContentViewerSandboxPage.MULTIPLE_DATASET = 'multiple';

class ContentViewer extends Widget {

  constructor(widgetElement) {
    super(widgetElement);
    this.widgetElement = widgetElement;
    this.pdfViewer = widgetElement.$('iframe');
  }

  waitForViewer() {
    browser.wait(EC.visibilityOf(this.widgetElement.$('.pdf-viewer-wrapper iframe')), DEFAULT_TIMEOUT);
  }

  getAsText() {
    // ignoreSynchronization is required because the iframe doesn't have angular inside
    browser.ignoreSynchronization = true;
    return browser.switchTo().frame(0).then(() => {
      return this.getPage(1).getText().then(function (text) {
        browser.switchTo().defaultContent();
        browser.ignoreSynchronization = false;
        return text;
      });
    });
  }

  getPage(number) {
    // wait for the page to load
    browser.wait(EC.presenceOf($('#pageContainer' + number + ' .endOfContent')), DEFAULT_TIMEOUT);
    return $('#pageContainer' + number);
  }

  isAlertPresent() {
    return this.widgetElement.$('.text-danger').isPresent();
  }

  isViewerPresent() {
    return this.pdfViewer.isPresent();
  }
}

class ContentViewerConfigDialog extends WidgetConfigDialog {

  constructor() {
    super(ContentViewer.WIDGET_NAME);
  }

}

ContentViewer.WIDGET_NAME = 'content-viewer';

module.exports.ContentViewerSandboxPage = ContentViewerSandboxPage;
module.exports.ContentViewer = ContentViewer;
module.exports.ContentViewerConfigDialog = ContentViewerConfigDialog;