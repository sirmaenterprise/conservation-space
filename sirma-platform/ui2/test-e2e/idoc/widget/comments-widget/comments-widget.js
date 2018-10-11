"use strict";
var Search = require('../../../search/components/search.js').Search;
var Widget = require('../widget').Widget;
var WidgetConfigDialog = require('../widget').WidgetConfigDialog;

class CommentsWidgetSandboxPage {
  open() {
    browser.get('/sandbox/idoc/widget/comments-widget/');
    this.widgetFrame = $('.comments-widget-content');
    this.waitUntilOpened(this.widgetFrame);
    this.modellingToggle = $('#modelling-toggle');
    this.showWidgetButton = $('#show-widget-button');
  }

  waitUntilOpened(element) {
    browser.wait(EC.visibilityOf(element), DEFAULT_TIMEOUT);
  }

  getWidget() {
    return new CommentsWidget($(`[widget="${CommentsWidget.WIDGET_NAME}"]`));
  }

  toggleModellingMode() {
    this.modellingToggle.click();
  }

  toggleCreateButton() {
    this.showWidgetButton.click();
  }
}

class CommentsWidget extends Widget {

  constructor(widgetElement) {
    super(widgetElement);
    this.widgetElement = widgetElement;
  }

  getCommentsSection() {
    browser.wait(EC.visibilityOf(this.widgetElement.$('.comments')), DEFAULT_TIMEOUT);
    return new CommentsWidgetCommentSection(this.widgetElement.$('.comments'));
  }

  getCommeentsWithoutWait() {
    return new CommentsWidgetCommentSection(this.widgetElement.$('.comments'));
  }

  getCommentsWidgetConfig() {
    return new CommentsWidgetConfigDialog();
  }

  getCommentsWidgetFooter() {
    return this.widgetElement.$('.comments-widget-footer');
  }

}


class CommentsWidgetCommentSection {
  constructor(element) {
    this.element = element;
  }

  waitUntilLoaded() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  hasComments() {
    return this.element.$('.comment').isPresent();
  }

  isDisplayed() {
    return this.element.isDisplayed();
  }
}

class CommentsWidgetConfigDialog extends WidgetConfigDialog {

  constructor() {
    super(CommentsWidget.WIDGET_NAME);
    this.selectObjectTab = $('.comments-widget-config .select-object-tab');
  }

  getSearch() {
    return new Search($(Search.COMPONENT_SELECTOR));
  }

  automaticallySelectObject() {
    $('.inline-group label').click();
    browser.wait(EC.visibilityOf($(Search.COMPONENT_SELECTOR)), DEFAULT_TIMEOUT);
  }

  includeCurrentObject() {
    $('.checkbox.show-first-page-only').click();
  }
}

CommentsWidget.WIDGET_NAME = 'comments-widget';

module.exports = {
  CommentsWidgetSandboxPage,
  CommentsWidget,
  CommentsWidgetConfigDialog
};