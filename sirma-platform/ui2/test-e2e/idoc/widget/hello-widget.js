"use strict";

var Widget = require('./widget').Widget;
var WidgetConfigDialog = require('./widget').WidgetConfigDialog;

class HelloWidget extends Widget {

  constructor(widgetElement) {
    super(widgetElement);

    this.titleElement = this.widgetElement.$('.hello-title');
  }

  getMessage() {
    return this.widgetElement.$('.hello-message').getText();
  }

  getTitle() {
    return this.titleElement.getText();
  }

}

class HelloWidgetConfigDialog extends WidgetConfigDialog {

  constructor() {
    super(HelloWidget.WIDGET_NAME);
  }

  setName(name) {
    var widgetNameInput = this.dialogElement.$('.name');
    browser.wait(EC.visibilityOf(widgetNameInput), DEFAULT_TIMEOUT);
    var script = 'var nameInput = $(arguments[0]); nameInput.val("' + name + '"); nameInput.trigger("change")';
    browser.executeScript(script, widgetNameInput.getWebElement());
    widgetNameInput.click();
    browser.wait(EC.textToBePresentInElementValue(widgetNameInput, name), DEFAULT_TIMEOUT);
    return this;
  }

}

HelloWidget.WIDGET_NAME = 'hello-widget';

module.exports.HelloWidget = HelloWidget;
module.exports.HelloWidgetConfigDialog = HelloWidgetConfigDialog;