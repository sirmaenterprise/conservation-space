import {Chart} from 'components/charts/chart';

class ChartImpl extends Chart {
  draw() {
  }
}

describe('Chart', () => {
  let element;
  before(() => {
    element = {
      find: () => {
        return {
          get: () => {
          },
          height: () => 30
        }
      }
    };
  });

  it('should throw error if draw method is not implemented', () => {
    expect(() => new Chart()).to.throw(Error, 'Must implement draw method in chart.');
  });

  it('should add tooltip', () => {
    let tooltipAdapter = {
      tooltip: sinon.spy()
    };
    let chart = new ChartImpl(element, tooltipAdapter);
    chart.addTooltip('element', 'A title (20) 20.00%');
    expect(tooltipAdapter.tooltip.callCount).to.equals(1);
    expect(tooltipAdapter.tooltip.getCall(0).args[1].title).to.equals('A title (20) 20.00%');
  });

  it('should clear remaining tooltips if any', () => {
    let tooltipAdapter = {
      clearTooltips: sinon.spy()
    };
    let chart = new ChartImpl(element, tooltipAdapter);
    chart.tooltipsRegistry = [1, 2, 3];
    chart.tooltipsRegistry.clear = sinon.spy();
    chart.clearTooltips();
    expect(chart.tooltipAdapter.clearTooltips.callCount).to.equals(1);
    expect(chart.tooltipAdapter.clearTooltips.getCall(0).args[0]).to.eql([1, 2, 3]);
    expect(chart.tooltipsRegistry.clear.callCount).to.equals(1);

  });

  describe('getChartSize', () => {
    it('should return size not higher than configured max size', () => {
      element.width = () => 600;
      let chart = new ChartImpl(element);
      chart.config = {
        maxSize: 400
      };
      expect(chart.getChartSize()).to.equals(330);
    });

    it('should return size not lower than 200', () => {
      element.width = () => 100;
      let chart = new ChartImpl(element);
      chart.config = {
        maxSize: 400
      };
      expect(chart.getChartSize()).to.equals(130);
    });

    it('should not exceed window height on print', () => {
      element.width = () => 600;
      let chart = new ChartImpl(element);
      chart.getWindowHeight = () => 400;
      chart.config = {
        maxSize: 600,
        isPrintMode: true
      };
      expect(chart.getChartSize()).to.equals(300);
    });
  });

  it('getTextFromHtml should strip all node elements and to return only the text', () => {
    let testHtml = '<div>Relatively long test label <span>(300)</span></div>';
    expect(Chart.getTextFromHtml(testHtml)).to.equals('Relative..');
    expect(Chart.getTextFromHtml(testHtml, 40)).to.equals('Relatively long test label (300)');
  });

  it('getTotal should return total number of results', () => {
    let data = [{
      value: 10
    }, {
      value: 7
    }, {
      value: 8
    }];
    expect(Chart.getTotal(data)).to.equals(25);
  });

  it('should redraw chart with new dimensions on print', () => {
    let chart = new ChartImpl(element);
    chart.redraw = sinon.spy();
    chart.addPrintListeners();
    chart.mediaQueryListener({matches: true});
    expect(chart.redraw.callCount).to.equals(1);
  });

  it('should redraw chart', () => {
    let tooltipAdapter = {
      clearTooltips: sinon.spy()
    };
    let chart = new ChartImpl(element, tooltipAdapter);
    let removeSpy = sinon.spy();
    chart.d3element = {
      selectAll: () => {
        return {
          remove: removeSpy
        }
      }
    };
    chart.draw = sinon.spy();
    chart.redraw();
    expect(removeSpy.callCount).to.equals(1);
    expect(chart.draw.callCount).to.equals(1);
    expect(chart.tooltipAdapter.clearTooltips.callCount).to.equals(1);
  });

  it('should deregister resize listener on destroy', () => {
    let chart = new ChartImpl(element);
    chart.clearTooltips = sinon.spy();
    chart.resizeListener = sinon.spy();
    chart.ngOnDestroy();
    expect(chart.clearTooltips.callCount).to.equals(1);
    expect(chart.resizeListener.callCount).to.equals(1);
  });
});
