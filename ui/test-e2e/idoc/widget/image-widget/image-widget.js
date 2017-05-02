"use strict";

var Widget = require('../widget').Widget;
var WidgetConfigDialog = require('../widget').WidgetConfigDialog;
var SandboxPage = require('../../../page-object').SandboxPage;
var Search = require('../../../search/components/search.js');
var SearchResults = require('../../../search/components/common/search-results').SearchResults;

class ImageWidgetSandboxPage extends SandboxPage {

  open() {
    super.open('/sandbox/idoc/widget/image-widget/');
    var widgetConfig = new ImageWidgetConfigDialog();
    var search = new Search(Search.COMPONENT_SELECTOR);
    search.waitUntilOpened();
    search.clickSearch();
    var results = new SearchResults('.modal .seip-search-wrapper .search-results');
    results.waitUntilOpened();
    results.clickResultItem(0);
    widgetConfig.save();
    this.widgetIframe = $('iframe');
    this.waitUntilOpened();
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.widgetIframe), DEFAULT_TIMEOUT);
  }

  getWidget() {
    return new ImageWidget($('.' + ImageWidget.WIDGET_NAME + ''));
  }
}

class ImageWidget extends Widget {

  constructor(widgetElement) {
    super(widgetElement);
    this.widgetElement = widgetElement;
  }

  switchToIframe() {
    browser.wait(EC.visibilityOf($('iframe')), DEFAULT_TIMEOUT);
    browser.ignoreSynchronization = true;
    return browser.switchTo().frame(0).then(function () {
      return new ImageWidgetMirador();
    });
  }

  switchToMainFrame() {
    return browser.switchTo().defaultContent().then(function () {
      browser.ignoreSynchronization = false;
      browser.waitForAngular();
    });

  }

  getCommentsSection() {
    browser.wait(EC.visibilityOf($('.image-comments-section')), DEFAULT_TIMEOUT);
    return new ImageWidgetCommentSection(this.widgetElement.$('.image-comments-section'));
  }
}

ImageWidget.frameName = 'mirador-viewer';

class ImageWidgetCommentSection {
  constructor(element) {
    this.element = element;
  }

  waitUntilLoaded() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  hasComments() {
    return this.element.$('.comment').isPresent();
  }
}

class ImageWidgetMirador {

  changeViewModeToGallery() {
    return browser.executeScript('$(arguments[0]).click();', $('li.thumbnails-option'));
  }

}

class ImageWidgetConfigDialog extends WidgetConfigDialog {

  constructor() {
    super(ImageWidget.WIDGET_NAME);
  }

}

ImageWidget.WIDGET_NAME = 'image-widget';

module.exports = {
  ImageWidgetSandboxPage,
  ImageWidget,
  ImageWidgetConfigDialog
};