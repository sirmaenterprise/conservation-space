import {View, Component, Inject, NgTimeout} from 'app/app';
import {Configurable} from 'components/configurable';
import {Keys} from 'common/keys';

import template from './datatable-numeric-filter.html!text';
import './datatable-numeric-filter.css!';

@Component({
  selector: 'seip-datatable-numeric-filter',
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
@Inject(NgTimeout)
export class DatatableNumericFilter extends Configurable {

  constructor($timeout) {
    super({});
    this.$timeout = $timeout;
  }

  onKeyPressed(event) {
    if (Keys.isEnter(event.keyCode)) {
      this.doFilter();
    }
  }

  onBlur() {
    if (!DatatableNumericFilter.isValidNumeric(this.value)) {
      this.value = null;
    }
  }

  doFilter() {
    if (DatatableNumericFilter.isValidNumeric(this.value)) {
      this.$timeout(() => {
        this.onFilter();
      });
    } else {
      this.value = null;
    }
  }

  static isValidNumeric(value) {
    return !isNaN(value);
  }
}
