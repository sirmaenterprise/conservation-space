import {View, Component, Inject, NgTimeout} from 'app/app';
import {Configurable} from 'components/configurable';
import {Keys} from 'common/keys';

import template from './datatable-string-filter.html!text';
import './datatable-string-filter.css!';

@Component({
  selector: 'seip-datatable-string-filter',
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
export class DatatableStringFilter extends Configurable {

  constructor($timeout) {
    super({});
    this.$timeout = $timeout;
  }

  onKeyPressed(event) {
    if (Keys.isEnter(event.keyCode)) {
      this.doFilter();
    }
  }

  doFilter() {
    this.$timeout(() => {
      this.onFilter();
    });
  }
}
