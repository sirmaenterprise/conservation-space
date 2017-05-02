'use strict';

var Widget = require('../widget').Widget;
var WidgetConfigDialog = require('../widget').WidgetConfigDialog;

var Search = require('../../../search/components/search.js');
var SearchResults = require('../../../search/components/common/search-results').SearchResults;

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
    return new Search('.seip-search-wrapper');
  }

  getSearchResults() {
    return new SearchResults('.search-results');
  }
}

ObjectLinkWidget.NAME = "object-link";
ObjectLinkWidget.COMMAND = "objectlink";

module.exports = {
  ObjectLinkWidget,
  ObjectLinkWidgetConfigDialog
};