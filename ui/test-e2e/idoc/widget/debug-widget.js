"use strict";

var Widget = require('./widget').Widget;
var WidgetConfigDialog = require('./widget').WidgetConfigDialog;

class DebugWidget extends Widget {

  constructor(widgetElement) {
    super(widgetElement);
  }

  getIdocMode() {
    return this.widgetElement.$('.idoc-mode').getText();
  }

  isModeling() {
    return this.widgetElement.$('.idoc-modeling').getText().then(function(text) {
      return text === 'true';
    });
  }

  getCurrentObjectType() {
    return this.widgetElement.$('.current-object-type').getText();
  }

  getCurrentObjectId() {
    return this.widgetElement.$('.current-object-id').getText();
  }

}

class DebugWidgetConfigDialog extends WidgetConfigDialog {

  constructor() {
    super(DebugWidget.WIDGET_NAME);
  }

}

DebugWidget.WIDGET_NAME = 'debug-widget';

module.exports.DebugWidget = DebugWidget;
module.exports.DebugWidgetConfigDialog = DebugWidgetConfigDialog;