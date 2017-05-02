import {View, Component, Inject, NgScope} from 'app/app';
import {Select} from 'components/select/select';
import {Configurable} from 'components/configurable';
import 'search/components/saved/save-search';

import 'font-awesome/css/font-awesome.css!';
import './search-toolbar.css!';
import toolbarTemplate from './search-toolbar.html!text';

export const ORDER_ASC = 'asc';
export const ORDER_DESC = 'desc';

/**
 * Container for results header - count, order by & sort by.
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'seip-search-toolbar',
  properties: {
    'results': 'results',
    'config': 'config',
    'callback': 'callback'
  }
})
@View({
  template: toolbarTemplate
})
@Inject(NgScope)
export class SearchToolbar extends Configurable {

  constructor($scope) {
    super({
      disabled: false,
      orderDirection: ORDER_DESC,
      labels: {
        total: 'search.messages.total',
        orderDirection: 'search.messages.orderDirection'
      },
      saveSearch: {
        render: false
      }
    });
    this.$scope = $scope;

    this.createOrderByConfig();
    this.bindSelectToConfig();
  }

  createOrderByConfig() {
    var orderByConfig = {
      width: '200px',
      hideSearchBox: true
    };

    if (this.config.orderByData) {
      orderByConfig.data = this.config.orderByData;
    } else if (this.config.orderByDataLoader) {
      orderByConfig.dataLoader = this.config.orderByDataLoader;
    }

    if (this.config.orderBy) {
      orderByConfig.defaultValue = this.config.orderBy;
    }
    this.orderByConfig = orderByConfig;
  }

  /**
   * Binds the order by select to the toolbar configuration. Example: If there is a search it will disable
   * the select or enable it if not.
   */
  bindSelectToConfig() {
    this.$scope.$watch(() => {
      return this.config.disabled;
    }, (newValue) => {
      this.orderByConfig.disabled = newValue;
    });
  }

  onOrderByChanged() {
    if (this.config.orderBy) {
      this.search();
    }
  }

  toggleOrderDirection() {
    if (this.config.orderDirection === ORDER_ASC) {
      this.config.orderDirection = ORDER_DESC;
    } else {
      this.config.orderDirection = ORDER_ASC;
    }
    this.search();
  }

  search() {
    this.callback({
      orderBy: this.config.orderBy,
      orderDirection: this.config.orderDirection
    });
  }

  isAscending() {
    return this.config.orderDirection === ORDER_ASC;
  }
}