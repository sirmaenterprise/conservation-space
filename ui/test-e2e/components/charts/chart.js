'use strict';

class Chart {
  constructor(element) {
    this.element = element;
    this.titleElement = this.element.$('.chart-title');
    this.totalElement = this.element.$('.total-count');
  }

  getTitle() {
    browser.wait(EC.visibilityOf(this.totalElement), DEFAULT_TIMEOUT);
    return this.titleElement.getText();
  }

  getTotalResults() {
    browser.wait(EC.visibilityOf(this.totalElement), DEFAULT_TIMEOUT);
    return this.totalElement.getText();
  }

  getXAxis() {
    return this.element.$('.x-axis');
  }

  getYAxis() {
    return this.element.$('.y-axis');
  }
}

module.exports.Chart = Chart;