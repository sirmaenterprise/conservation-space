import {PieChart} from 'components/charts/pie-chart/pie-chart';

describe('PieChart', () => {
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
    let pieChart = new PieChart(element);
    pieChart.total = 100;
    expect(pieChart.getTooltipText({data: {label: 'Title', value: 20}})).to.equals('Title (20) 20.00%');
  });
});
