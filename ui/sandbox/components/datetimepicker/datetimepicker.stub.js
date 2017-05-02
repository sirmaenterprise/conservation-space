import {Component, View, Inject} from 'app/app';
import datetimepickerTemplateStub from 'datetimepicker-template!text';
import {Datetimepicker} from 'components/datetimepicker/datetimepicker';

@Component({
  selector: 'seip-datetimepicker-stub'
})
@View({
  template: datetimepickerTemplateStub
})

export class DatetimepickerStub {
  constructor() {

  }

  setToday() {
    this.picker1 = new Date().toISOString();
  }

  clear() {
    this.picker1 = '';
  }
}
