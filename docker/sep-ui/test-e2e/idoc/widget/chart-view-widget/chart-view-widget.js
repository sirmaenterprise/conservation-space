'use strict';

var Widget = require('../widget').Widget;
var WidgetConfigDialog = require('../widget').WidgetConfigDialog;
var ObjectSelector = require('../object-selector/object-selector.js').ObjectSelector;
var ChartConfigTab = require('./chart-config-tab.js').ChartConfigTab;
var SandboxPage = require('../../../page-object').SandboxPage;

class ChartViewWidgetSandboxPage extends SandboxPage {

  constructor() {
    super();
    this.widgetToggle = $('#toggle-widget');
    this.modellingToggle = $('#modeling');
  }

  open() {
    super.open('/sandbox/idoc/widget/chart-view');
    browser.wait(EC.visibilityOf(this.widgetToggle), DEFAULT_TIMEOUT);
  }

  toggleModellingMode() {
    this.modellingToggle.click();
  }

  toggleWidget() {
    this.widgetToggle.click();
  }

  getChartWrapper() {
    return $('.chart-wrapper');
  }

  getErrorMessageWrapper() {
    return $('.error');
  }
}

class ChartViewWidget extends Widget {
  constructor(widgetElement) {
    super(widgetElement);
    this.waitUntilOpened();
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.getChartParent()), DEFAULT_TIMEOUT);
  }

  getChartParent() {
    return this.widgetElement.$('.chart-view');
  }
}

class ChartViewWidgetConfigDialog extends WidgetConfigDialog {
  constructor() {
    super(ChartViewWidget.WIDGET_NAME);
    this.selectObjectTab = $('.chart-view-widget-config .select-object-tab-handler');
    this.chartConfigOptionsTab = $('.chart-view-widget-config .chart-configuration-tab-handler');
  }

  selectObjectSelectTab() {
    this.selectObjectTab.click();
    return new ObjectSelector();
  }

  selectChartConfiguration() {
    this.chartConfigOptionsTab.click();
    let chartConfigTab = new ChartConfigTab();
    chartConfigTab.waitUntilOpened();
    return chartConfigTab;
  }
}

ChartViewWidget.WIDGET_NAME = 'chart-view-widget';
module.exports.ChartViewWidget = ChartViewWidget;
module.exports.ChartViewWidgetConfigDialog = ChartViewWidgetConfigDialog;
module.exports.ChartViewWidgetSandboxPage = ChartViewWidgetSandboxPage;