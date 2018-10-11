import {View, Component, Inject, NgTimeout} from 'app/app';
import _ from 'lodash';
import {Configurable} from 'components/configurable';
import {ContextualObjectsFactory} from 'services/context/contextual-objects-factory';
import 'components/select/object/object-select';
import 'components/select/select';

import template from './datatable-object-filter.html!text';
import './datatable-object-filter.css!';

@Component({
  selector: 'seip-datatable-object-filter',
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
@Inject(NgTimeout, ContextualObjectsFactory)
export class DatatableObjectFilter extends Configurable {

  constructor($timeout, contextualObjectsFactory) {
    super({});
    this.$timeout = $timeout;
    this.contextualObjectsFactory = contextualObjectsFactory;

    if (this.config.aggregated) {
      this.availableValues = this.config.aggregated[this.header.uri];
    }

    if (_.isEmpty(this.availableValues)) {
      this.availableValues = undefined;
      this.createSelectConfig();
    } else {
      this.createObjectSelectConfig();
    }
  }

  createSelectConfig() {
    this.selectConfig = {
      data: []
    };
  };

  createObjectSelectConfig() {
    this.selectConfig = {
      predefinedItems: [
        this.contextualObjectsFactory.getAnyObject(),
        this.contextualObjectsFactory.getCurrentObject()
      ],
      availableObjects: Object.keys(this.availableValues)
    };
  }

  onChanged() {
    this.$timeout(() => {
      this.onFilter();
    });
  }
}
