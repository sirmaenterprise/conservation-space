'use strict';

var SingleSelectMenu = require('../../../form-builder/form-control').SingleSelectMenu;

class ChartConfigTab {
  constructor() {
    this.element = $('.chart-configuration-tab');
    var groupBySelectElement = this.element.$('.group-by-select');
    browser.wait(EC.visibilityOf(groupBySelectElement), DEFAULT_TIMEOUT);
    this.groupBySelect = new SingleSelectMenu(groupBySelectElement);

    var chartTypeSelectElement = this.element.$('.chart-type-select');
    browser.wait(EC.visibilityOf(chartTypeSelectElement), DEFAULT_TIMEOUT);
    this.chartTypeSelect = new SingleSelectMenu(chartTypeSelectElement);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  selectGroupBy(name) {
    this.groupBySelect.selectOption(name);
  }

  selectChartType(name) {
    this.chartTypeSelect.selectOption(name);
  }
}

module.exports.ChartConfigTab = ChartConfigTab;