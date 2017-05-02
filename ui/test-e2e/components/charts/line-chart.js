'use strict';

var SandboxPage = require('../../page-object').SandboxPage;
var Chart = require('./chart').Chart;

class LineChartSandboxPage extends SandboxPage {
  constructor() {
    super();
  }

  open() {
    super.open('/sandbox/components/charts/line-chart');
    browser.wait(EC.visibilityOf($('.chart-parent')), DEFAULT_TIMEOUT);
    this.chart = new LineChart($('.chart-parent'));
  }
}

class LineChart extends Chart {
  constructor(parentElement) {
    super(parentElement.$('.chart-wrapper'));
    this.chartElement = this.element.$('svg.line-chart');
    this.waitUntilOpened();
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.chartElement), DEFAULT_TIMEOUT);
  }

  isLineVisible() {
    return this.chartElement.$('path.line').isDisplayed();
  }
}

LineChart.CHART_NAME = 'line-chart';
module.exports.LineChart = LineChart;
module.exports.LineChartSandboxPage = LineChartSandboxPage;