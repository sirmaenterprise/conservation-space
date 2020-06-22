'use strict';

var Widget = require('../widget').Widget;
var WidgetConfigDialog = require('../widget').WidgetConfigDialog;
var WidgetOptionsTab = require('./widget-options-tab.js').WidgetOptionsTab;
var ReportConfigTab = require('./report-config-tab.js').ReportConfigTab;
var ObjectSelector = require('./../object-selector/object-selector.js').ObjectSelector;

class AggregatedTableSandboxPage {
  open() {
    browser.get('/sandbox/idoc/widget/aggregated-table');
    this.widgetFrame = $('.aggregated-table-content');
    this.waitUntilOpened(this.widgetFrame);
    this.modellingToggle = $('#modelling-toggle');
    this.showWidgetButton = $('#toggle-widget');
  }

  waitUntilOpened(element) {
    if (element) {
      browser.wait(EC.visibilityOf(element), DEFAULT_TIMEOUT);
    } else {
      browser.wait(EC.visibilityOf(this.insertButton), DEFAULT_TIMEOUT);
    }
  }

  insertWidget() {
    if (this.insertButton) {
      this.insertButton.click();
    } else {
      this.showWidgetButton.click();
    }
  }

  getWidget() {
    return new AggregatedTable($('.aggregated-table'));
  }

  toggleModellingMode() {
    this.modellingToggle.click();
    browser.wait(EC.elementToBeSelected(element(by.id('modelling-toggle'))), DEFAULT_TIMEOUT);
  }
}

class AggregatedTable extends Widget {

  constructor(widgetElement) {
    super(widgetElement);
  }

  getRow(index) {
    return new AggregatedTableRow(this.widgetElement.$('.form-wrapper:nth-child(' + index + ')'));
  }

  getRowCount() {
    return element.all(by.css('form')).count();
  }

}

class AggregatedTableRow {
  constructor(element) {
    this.element = element;
  }
}

class AggregatedTableConfigDialog extends WidgetConfigDialog {

  constructor() {
    super(AggregatedTable.WIDGET_NAME);
    this.selectObjectTab = $('.aggregatedtable-widget-config .select-object-tab-handler');
    this.displayOptionsTab = $('.aggregatedtable-widget-config .display-options-tab-handler');
    this.reportConfigOptionsTab = $('.aggregatedtable-widget-config .report-configuration-tab-handler');
  }

  selectObjectSelectTab() {
    this.selectObjectTab.click();
    return new ObjectSelector();
  }

  selectDisplayOptionTab() {
    this.displayOptionsTab.click();
    let widgetOptionsTab = new WidgetOptionsTab();
    widgetOptionsTab.waitUntilOpened();
    return widgetOptionsTab;
  }

  selectReportConfiguration() {
    this.reportConfigOptionsTab.click();
    let reportConfigTab = new ReportConfigTab();
    reportConfigTab.waitUntilOpened();
    return reportConfigTab;
  }

  selectAutomaticallySelect() {
    element.all(by.css('.inline-group label')).get(0).click();
    browser.wait(EC.visibilityOf($('.seip-search-wrapper')), DEFAULT_TIMEOUT);
  }

  save(flag) {
    if (!flag) {
      super.save();
    } else {
      var okButton = this.dialogElement.$('.seip-btn-ok');
      browser.wait(EC.elementToBeClickable(okButton), DEFAULT_TIMEOUT);
      okButton.click();
      browser.wait(EC.visibilityOf($('.aggregated-table-widget')), DEFAULT_TIMEOUT);
    }
  }
}

AggregatedTable.WIDGET_NAME = 'aggregated-table';

module.exports.AggregatedTableSandboxPage = AggregatedTableSandboxPage;
module.exports.AggregatedTable = AggregatedTable;
module.exports.AggregatedTableConfigDialog = AggregatedTableConfigDialog;
