import {View, Component, Inject, NgTimeout} from 'app/app';
import {Configurable} from 'components/configurable';
import {TranslateService} from 'services/i18n/translate-service';
import 'components/select/select';

import template from './datatable-boolean-filter.html!text';
import './datatable-boolean-filter.css!';

@Component({
  selector: 'seip-datatable-boolean-filter',
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
@Inject(NgTimeout, TranslateService)
export class DatatableBooleanFilter extends Configurable {
  constructor($timeout, translateService) {
    super({});
    this.$timeout = $timeout;
    this.translateService = translateService;
  }

  ngOnInit() {
    this.createSelectConfig();
  }

  createSelectConfig() {
    this.selectConfig = {
      multiple: true,
      data: this.getAvailableValues(),
      defaultValue: this.value,
      isDisabled: this.config.isDisabled
    };
  }

  getAvailableValues() {
    let selectValues = [{
      id: true,
      text: this.translateService.translateInstant('search.advanced.value.boolean.true')
    }, {
      id: false,
      text: this.translateService.translateInstant('search.advanced.value.boolean.false')
    }];
    let availableValues;
    if (this.config.aggregated && (availableValues = this.config.aggregated[this.header.uri])) {
      selectValues = selectValues.filter((selectValue) => {
        return availableValues[selectValue.id];
      });
    }
    return selectValues;
  }

  onChanged() {
    this.$timeout(() => {
      this.onFilter();
    });
  }
}
