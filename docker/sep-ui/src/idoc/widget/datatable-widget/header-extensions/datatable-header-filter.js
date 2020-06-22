import {View, Component} from 'app/app';

import './datatable-header-filter.css!';
import template from './datatable-header-filter.html!text';

@Component({
  selector: 'seip-datatable-header-filter',
  properties: {
    context: 'context',
    control: 'control',
    config: 'config'
  }
})
@View({
  template
})
export class DatatableHeaderFilter {

  constructor() {
    this.context.getCurrentObject().then((currentObject) => {
      this.showButton = !currentObject.isVersion() && this.config.insertFilterRow;
    });
  }

  toggleFilterRow() {
    this.control.publish('toggleFilterRow', {});
  }
}
