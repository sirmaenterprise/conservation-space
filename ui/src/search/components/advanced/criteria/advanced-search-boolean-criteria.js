import {Component, View, Inject, NgScope} from 'app/app';
import {Configurable} from 'components/configurable';
import {TranslateService} from 'services/i18n/translate-service';

import template from './advanced-search-boolean-criteria.html!text';

@Component({
  selector: 'seip-advanced-search-boolean-criteria',
  properties: {
    'config': 'config',
    'criteria': 'criteria'
  }
})
@View({
  template: template
})
@Inject(NgScope, TranslateService)
export class AdvancedSearchBooleanCriteria extends Configurable {

  constructor($scope, translateService) {
    super({
      disabled: false
    });
    this.$scope = $scope;
    this.translateService = translateService;
    this.createSelectConfig();
    this.registerDisabledWatcher();
  }

  createSelectConfig() {
    this.selectConfig = {
      defaultValue: 'true',
      multiple: false,
      data: [{
        id: 'true',
        text: this.translateService.translateInstant('search.advanced.value.boolean.true')
      }, {
        id: 'false',
        text: this.translateService.translateInstant('search.advanced.value.boolean.false')
      }],
      disabled: this.config.disabled,
      selectOnClose: true
    };

    if (this.criteria.value === 'false') {
      this.selectConfig.defaultValue = this.criteria.value;
    }
  }

  registerDisabledWatcher() {
    this.$scope.$watch(()=> {
      return this.config.disabled;
    }, (state) => {
      this.selectConfig.disabled = state;
    });
  }

}