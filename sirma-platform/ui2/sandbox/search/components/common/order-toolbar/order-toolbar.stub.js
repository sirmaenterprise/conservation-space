import {Component, View} from 'app/app';
import {ORDER_DESC} from 'search/order-constants';
import 'search/components/common/order-toolbar';

import 'font-awesome';
import template from 'order-toolbar-template!text';

@Component({
  selector: 'seip-order-toolbar-stub'
})
@View({
  template: template
})
export class OrderToolbarStub {

  constructor() {
    this.disabled = false;

    this.orderBy = 0;
    this.orderDirection = ORDER_DESC;

    this.config = {
      orderByData: [
        {id: 0, text: 'zero', disabled: false},
        {id: 1, text: 'one', disabled: false},
        {id: 2, text: 'two', disabled: false},
        {id: 3, text: 'three', disabled: false},
        {id: 4, text: 'four', disabled: true},
      ],
      orderBy: this.orderBy,
      orderByDirection: this.orderDirection
    };
  }

  toggleToolbar() {
    this.disabled = !this.disabled;
  }

  onOrderChanged(params) {
    this.orderBy = params.orderBy;
    this.orderDirection = params.orderDirection;
  }
}