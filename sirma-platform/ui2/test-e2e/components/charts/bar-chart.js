'use strict';

var Chart = require('./chart').Chart;

class BarChart extends Chart {
  constructor(parentElement) {
    super(parentElement.$('.chart-wrapper'));
    this.chartElement = this.element.$('svg.bar-chart');
    this.waitUntilOpened();
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.chartElement), DEFAULT_TIMEOUT);
  }

  getNumberOfBars() {
    return this.chartElement.$$('.bar').count();
  }
}

BarChart.CHART_NAME = 'bar-chart';
module.exports.BarChart = BarChart;