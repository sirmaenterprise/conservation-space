"use strict";

var Widget = require('../widget').Widget;
var WidgetConfigDialog = require('../widget').WidgetConfigDialog;
var SandboxPage = require('../../../page-object').SandboxPage;
var Search = require('../../../search/components/search.js').Search;

class ImageWidgetSandboxPage extends SandboxPage {

  open() {
    super.open('/sandbox/idoc/widget/image-widget/');
    var widgetConfig = new ImageWidgetConfigDialog();
    var search = new Search($(Search.COMPONENT_SELECTOR));
    search.getCriteria().getSearchBar().search();
    var results = search.getResults();
    results.waitForResults();
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

  openWidgetConfig() {
    this.widgetElement.$('.config-button').click();
    return new ImageWidgetConfigDialog(ImageWidget.WIDGET_NAME);
  }

  getViewer() {
    return this.widgetElement.$('.image-widget-viewer');
  }

  getCommentsSection() {
    browser.wait(EC.presenceOf($('.image-comments')), DEFAULT_TIMEOUT);
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
    return this.element.$('.comment').isPresent().then((present) => {
      if (present) {
        // some functionality just hides the comments section, so it needs to be checked if its displayed too.
        return this.element.$('.comment').isDisplayed();
      }
      return false;
    });
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

  toggleWidgetDisplayOptions() {
    this.dialogElement.$('li.display-options-tab-handler').click();
    browser.wait(EC.visibilityOf($('section.display-options-tab .image-configuration')), DEFAULT_TIMEOUT)
    return this;
  }

  toggleHideAnnotations() {
    this.dialogElement.$('.hide-annotations').click();
  }

  toggleLockWidget() {
    this.dialogElement.$('.lock-widget').click();
  }
}

ImageWidget.WIDGET_NAME = 'image-widget';

module.exports = {
  ImageWidgetSandboxPage,
  ImageWidget,
  ImageWidgetConfigDialog
};