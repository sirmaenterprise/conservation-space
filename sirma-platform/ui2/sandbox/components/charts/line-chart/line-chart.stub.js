import {Component, View, Inject} from 'app/app';
import {LineChart} from 'components/charts/line-chart/line-chart';
import template from './line-chart.stub.html!text';

@Component({
  selector: 'seip-line-chart-stub'
})
@View({
  template: template
})
@Inject()
export class LineChartStub {
  constructor() {
    this.config = {
      maxSize: 800,
      data: [
        {
          name: 'value1',
          label: 'Value 1',
          value: 22
        },
        {
          name: 'value2',
          label: 'Value 2',
          value: 58
        },
        {
          name: 'value3',
          label: 'Value 3',
          value: 13
        }
      ]
    };
  }
}
