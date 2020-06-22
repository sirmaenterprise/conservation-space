import {Component, View, Inject} from 'app/app';
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
@View({template})
@Inject(TranslateService)
export class AdvancedSearchBooleanCriteria extends Configurable {

  constructor(translateService) {
    super({
      disabled: false
    });
    this.translateService = translateService;
  }

  ngOnInit() {
    this.createSelectConfig();
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
      isDisabled: () => this.config.disabled,
      selectOnClose: true
    };

    if (this.criteria.value === 'false') {
      this.selectConfig.defaultValue = this.criteria.value;
    }
  }

}
