'use strict';

var Chart = require('./chart').Chart;

class PieChart extends Chart {
  constructor(parentElement) {
    super(parentElement.$('.chart-wrapper'));
    this.chartElement = this.element.$('svg.pie-chart');
    this.waitUntilOpened();
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.chartElement), DEFAULT_TIMEOUT);
  }

  getNumberOfArcs() {
    return this.chartElement.$$('.arc').count();
  }
}

PieChart.CHART_NAME = 'pie-chart';
module.exports.PieChart = PieChart;
