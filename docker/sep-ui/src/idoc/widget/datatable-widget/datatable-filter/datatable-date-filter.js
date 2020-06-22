import {View, Component, Inject, NgTimeout} from 'app/app';
import {Configurable} from 'components/configurable';
import {Configuration} from 'common/application-config';
import 'components/datetimepicker/datetimepicker';
import {MomentAdapter} from 'adapters/moment-adapter';

import template from './datatable-date-filter.html!text';
import './datatable-date-filter.css!';

@Component({
  selector: 'seip-datatable-date-filter',
  properties: {
    config: 'config',
    header: 'header',
    value: 'value'
  },
  events: ['onFilter']
})
@View({
  template
})
@Inject(NgTimeout, Configuration, MomentAdapter)
export class DatatableDateFilter extends Configurable {

  constructor($timeout, configuration, momentAdapter) {
    super({});
    this.$timeout = $timeout;
    this.momentAdapter = momentAdapter;

    this.pickerConfig = {
      dateFormat: configuration.get(Configuration.UI_DATE_FORMAT),
      timeFormat: configuration.get(Configuration.UI_TIME_FORMAT),
      hideTime: true,
      useCurrent: false,
      defaultValue: this.value && this.value.length > 0 ? this.value[0] : '',
      isDisabled: this.config.isDisabled,
      widgetParent: 'body'
    };
  }

  onChanged(newValue) {
    if (newValue) {
      let date = this.momentAdapter.parse(newValue);
      let startDate = date.clone().startOf('day');
      let endDate = date.clone().endOf('day');
      this.value = [startDate.toISOString(), endDate.toISOString()];
    } else {
      this.value = undefined;
    }
    this.$timeout(() => {
      this.onFilter();
    });
  }
}
