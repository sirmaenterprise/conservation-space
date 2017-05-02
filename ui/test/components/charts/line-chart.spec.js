import {LineChart} from 'components/charts/line-chart/line-chart';

describe('LineChart', () => {
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
    let chart = new LineChart(element);
    chart.total = 100;
    expect(chart.getTooltipText({label: 'Title', value: 20})).to.equals('Title (20) 20.00%');
  });
});
