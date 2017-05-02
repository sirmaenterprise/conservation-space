'use strict';

var LineChartSandboxPage = require('./line-chart').LineChartSandboxPage;

describe('LineChart', () => {
  it('should display tooltip when hover over the chart', () => {
    var sandboxPage = new LineChartSandboxPage();
    sandboxPage.open();

    browser.wait(sandboxPage.chart.getXAxis(), DEFAULT_TIMEOUT);
    browser.wait(sandboxPage.chart.getYAxis(), DEFAULT_TIMEOUT);

    Promise.all([sandboxPage.chart.getXAxis().getSize(), sandboxPage.chart.getYAxis().getSize()]).then((result) => {
      // Chart has 3 values and mouse hovering is divided into 4 equal width segments. Hovering on the first will display tooltip for the first value.
      // Hovering on the second or third will display tooltip for second value and hovering on the fourth will display tooltip for the third value.
      let offsetX = result[0].width / 4;
      // Middle of the y axis
      let offsetY = result[1].height / 2;
      // position mouse in the first segment
      browser.actions().mouseMove(sandboxPage.chart.getXAxis(), {x: parseInt(offsetX), y: -parseInt(offsetY)}).perform();
      let tooltipElement = $('.tooltip');
      expect(tooltipElement.isPresent()).to.eventually.be.true;
      expect(tooltipElement.getText()).to.eventually.equals('Value 1 (22) 23.66%');
    });
  });
});