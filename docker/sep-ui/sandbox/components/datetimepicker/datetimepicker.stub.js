import {Component, View, Inject} from 'app/app';
import {Datetimepicker} from 'components/datetimepicker/datetimepicker';
import {MomentAdapter} from 'adapters/moment-adapter';
import datetimepickerTemplateStub from 'datetimepicker-template!text';

@Component({
  selector: 'seip-datetimepicker-stub'
})
@View({
  template: datetimepickerTemplateStub
})
@Inject(MomentAdapter)
export class DatetimepickerStub {
  constructor(momentAdapter) {
    this.todayFormatted = momentAdapter.parse(new Date().toISOString()).format('MMMM/DD/YYYY');
  }

  setToday() {
    this.picker1 = new Date().toISOString();
  }

  clear() {
    this.picker1 = '';
  }
}
