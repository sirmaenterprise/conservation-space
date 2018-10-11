'use strict';

let Widget = require('../widget').Widget;
let WidgetConfigDialog = require('../widget').WidgetConfigDialog;
let ObjectSelector = require('./../object-selector/object-selector.js').ObjectSelector;
let SandboxPage = require('../../../page-object').SandboxPage;
let DisplayOptions = require('../object-data-widget/display-options').DisplayOptions;
let Search = require('../../../search/components/search.js').Search;


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

  getPdfViewer() {
    return new PdfViewer($('.widget[widget="content-viewer"]'));
  }

  getVideoPlayer() {
    return new VideoPlayer($('.widget[widget="content-viewer"]'));
  }

  getAudioPlayer() {
    return new AudioPlayer($('.widget[widget="content-viewer"]'));
  }

  getImageViewer() {
    return new ImageViewer($('.widget[widget="content-viewer"]'));
  }
}

ContentViewerSandboxPage.EMPTY_DATASET = 'empty';
ContentViewerSandboxPage.SINGLE_DATASET = 'single';
ContentViewerSandboxPage.MULTIPLE_DATASET = 'multiple';

class ContentViewer extends Widget {

  constructor(widgetElement) {
    super(widgetElement);
    this.widgetElement = widgetElement;
  }

}

class PdfViewer extends ContentViewer {

  constructor(widgetElement) {
    super(widgetElement);
    this.pdfViewer = widgetElement.$('iframe');
  }

  waitForViewer() {
    browser.wait(EC.visibilityOf(this.widgetElement.$('.pdf-viewer-wrapper iframe')), DEFAULT_TIMEOUT);
  }

  getPageAsText(pageNumber) {
    // ignoreSynchronization is required because the iframe doesn't have angular inside
    browser.ignoreSynchronization = true;
    return browser.switchTo().frame(0).then(() => {
      return this.getPage(pageNumber).getText().then(function (text) {
        browser.switchTo().defaultContent();
        browser.ignoreSynchronization = false;
        return text;
      });
    });
  }

  getPage(number) {
    browser.wait(EC.presenceOf($('#viewer [data-page-number="' + number + '"] .endOfContent')), DEFAULT_TIMEOUT);
    return $('#viewer [data-page-number="' + number + '"] .textLayer');
  }

  isViewerPresent() {
    return this.pdfViewer.isPresent();
  }
}

class VideoPlayer extends ContentViewer {
  constructor(widgetElement) {
    super(widgetElement);
    this.viewer = widgetElement.$('video');
  }

  waitForViewer() {
    browser.wait(EC.visibilityOf(this.viewer), DEFAULT_TIMEOUT);
  }

  isViewerPresent() {
    return this.viewer.isPresent();
  }
}

class AudioPlayer extends ContentViewer {
  constructor(widgetElement) {
    super(widgetElement);
    this.viewer = widgetElement.$('audio');
  }

  waitForViewer() {
    browser.wait(EC.visibilityOf(this.viewer), DEFAULT_TIMEOUT);
  }

  isViewerPresent() {
    return this.viewer.isPresent();
  }
}

class ImageViewer extends ContentViewer {
  constructor(widgetElement) {
    super(widgetElement);
    this.viewer = widgetElement.$('img');
  }

  waitForViewer() {
    browser.wait(EC.visibilityOf(this.viewer), DEFAULT_TIMEOUT);
  }

  isViewerPresent() {
    return this.viewer.isPresent();
  }
}

class ContentViewerConfigDialog extends WidgetConfigDialog {

  constructor() {
    super(ContentViewer.WIDGET_NAME);
    this.dialogElement = $('.seip-modal');
    this.waitUntilOpened();
    this.selectObjectTab = $$('.content-viewer-widget a').get(0);
    this.displayOptionsTab = $$('.content-viewer-widget a').get(1);
  }

  selectObjectSelectTab() {
    browser.wait(EC.visibilityOf(this.selectObjectTab), DEFAULT_TIMEOUT);
    this.selectObjectTab.click();
    return new ObjectSelector();
  }

  selectDisplayOptionsTab() {
    browser.wait(EC.visibilityOf(this.displayOptionsTab), DEFAULT_TIMEOUT);
    this.displayOptionsTab.click();
    let displayOptions = new DisplayOptions();
    displayOptions.waitUntilOpened();
    return displayOptions;
  }
  getSearch() {
    return new Search($(Search.COMPONENT_SELECTOR));
  }

  save() {
    let okButton = this.dialogElement.$('.seip-btn-ok');
    browser.wait(EC.elementToBeClickable(okButton), DEFAULT_TIMEOUT);
    okButton.click();
    this.waitUntilClosed();
    this.waitForWidget();
  }

  waitForWidget() {
    new Widget($(this.widgetSelector)).waitToAppear();
  }

  waitUntilClosed() {
    browser.wait(EC.not(EC.presenceOf(this.dialogElement)), DEFAULT_TIMEOUT);
  }

}

ContentViewer.WIDGET_NAME = 'content-viewer';

module.exports.ContentViewerSandboxPage = ContentViewerSandboxPage;
module.exports.ContentViewer = ContentViewer;
module.exports.PdfViewer = PdfViewer;
module.exports.VideoPlayer = VideoPlayer;
module.exports.AudioPlayer = AudioPlayer;
module.exports.ImageViewer = ImageViewer;
module.exports.ContentViewerConfigDialog = ContentViewerConfigDialog;