import {BarChart} from 'components/charts/bar-chart/bar-chart';

describe('BarChart', () => {
  let element;
  before(() => {
    element = {
      find: () => {
        return {
          get: () => {},
          height: () => 30
        }
      }
    };
  });

  it('should generate proper tooltip text', () => {
    let chart = new BarChart(element);
    chart.total = 100;
    expect(chart.getTooltipText({label: 'Title', value: 20})).to.equals('Title (20) 20.00%');
  });
});
