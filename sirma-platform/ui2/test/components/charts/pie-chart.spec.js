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

  it('should not change rotation of label if it is right side up', () => {
    let d = {
      startAngle: 0.5,
      endAngle: 1.8
    };
    expect(Math.round(PieChart.getAngle(d))).to.equals(-24);
  });

  it('should flip label if it is upside down', () => {
    let d = {
      startAngle: 3.6,
      endAngle: 3.14
    };
    expect(Math.round(PieChart.getAngle(d))).to.equals(-77);
  });
});
