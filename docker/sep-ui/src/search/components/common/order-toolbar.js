import {Component, View} from 'app/app';
import {ORDER_ASC, ORDER_DESC} from 'search/order-constants';
import _ from 'lodash';

import './order-toolbar.css!';
import toolbarTemplate from './order-toolbar.html!text';

/**
 *  Simple drop down oriented order by menu component. Which wraps two main order
 *  by values - order by option & order by direction. This component serves to
 *  mainly visualise the selected order by option & direction and let the client
 *  interact with it in a convinient way. This component accepts no configuration
 *  but simply the values for the order by option & direction, as well as the
 *  order by data which contains all order by options as an array. Each element can
 *  be either enabled or disabled, if an option is disabled it can not be selected
 *  and the component will appropriately visualise it as disabled - appending custom
 *  styles. Each time the order by option & direction are changed a component event is
 *  triggered: onOrderChanged. An example input data properties to this component:
 *
 *  orderBy: 'title_id'
 *  orderByDirection: ORDER_ASC
 *  orderByData: [
 *    {
 *      id: 'title_id',
 *      text: 'Title',
 *      disabled: false
 *    },
 *    {
 *      id: 'type_id',
 *      text: 'Type',
 *      disabled: false
 *    },
 *    {
 *      id: 'data_id',
 *      text: 'Date',
 *      disabled: false
 *    }
 *  ]
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'seip-order-toolbar',
  properties: {
    'disabled': 'disabled',
    'orderBy': 'orderBy',
    'orderByData': 'orderByData',
    'orderDirection': 'orderDirection'
  },
  events: ['onOrderChanged']
})
@View({
  template: toolbarTemplate
})
export class OrderToolbar {

  getOrderByValue() {
    if (this.orderBy !== this.previousOrderBy) {
      let found = _.find(this.orderByData, order => {
        return order.id === this.orderBy;
      });

      if (found) {
        this.order = found.text;
        this.previousOrderBy = this.orderBy;
      }
    }
    return this.order;
  }

  onOrderSelected(order) {
    if (!order.disabled) {
      this.orderBy = order.id;
      this.onOrderChanged(this.getParams());
    }
  }

  onOrderToggled() {
    this.orderDirection = this.isAscending() ? ORDER_DESC : ORDER_ASC;
    this.onOrderChanged(this.getParams());
  }

  isAscending() {
    return this.orderDirection === ORDER_ASC;
  }

  getParams() {
    return {
      params: {
        orderBy: this.orderBy,
        orderDirection: this.orderDirection
      }
    };
  }
}