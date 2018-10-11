'use strict';

var Widget = require('../widget').Widget;
var WidgetConfigDialog = require('../widget').WidgetConfigDialog;

var Search = require('../../../search/components/search.js').Search;

class ObjectLinkWidgetSandboxPage {

  open() {
    browser.get('/sandbox/idoc/widget/object-link-widget/');
    this.insertButton = $('#insert-widget');
    browser.wait(EC.visibilityOf(element), DEFAULT_TIMEOUT);
  }

  getWidget() {
    return new ObjectLinkWidget($('.' + ObjectLinkWidget.WIDGET_NAME + ''));
  }
}

class ObjectLinkWidget extends Widget {

  constructor(widgetElement) {
    super(widgetElement);
    this.widgetElement = widgetElement;
  }

  getHeader() {
    return this.widgetElement.$('.instance-header');
  }
}

class ObjectLinkWidgetConfigDialog extends WidgetConfigDialog {

  constructor() {
    super(ObjectLinkWidget.NAME);
  }

  getSearch() {
    return new Search($(Search.COMPONENT_SELECTOR));
  }
}

ObjectLinkWidget.NAME = "object-link";
ObjectLinkWidget.COMMAND = "objectlink";

module.exports = {
  ObjectLinkWidget,
  ObjectLinkWidgetConfigDialog
};