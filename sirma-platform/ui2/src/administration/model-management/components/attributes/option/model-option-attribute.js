import {View, Component, Inject} from 'app/app';
import {TranslateService} from 'services/i18n/translate-service';
import {ModelGenericAttribute} from 'administration/model-management/components/attributes/model-generic-attribute';
import _ from 'lodash';

import 'administration/model-management/components/attributes/select/model-select-attribute';

import template from './model-option-attribute.html!text';

/**
 * Component rendering dropdown of values provided as options in the meta data of the provided attribute. Options
 * should be present in the meta data in order to be properly loaded and rendered. They are represented by an array
 * of objects with two properties: id and label key. The label key is then translated using the translate service.
 * Labels for options should be present in order to be translated properly.
 *
 * Format of passed options:
 * [
 *    {
 *       value: "value of option one",
 *       label: "label key of option one"
 *    }, {
 *       value: "value of option two",
 *       label: "label key of option two"
 *    }
 * ]
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'model-option-attribute',
  properties: {
    'config': 'config',
    'context': 'context',
    'editable': 'editable',
    'attribute': 'attribute'
  },
  events: ['onChange']
})
@View({
  template
})
@Inject(TranslateService)
export class ModelOptionAttribute extends ModelGenericAttribute {

  constructor(translateService) {
    super();
    this.translateService = translateService;
  }

  ngOnInit() {
    this.initSelectConfig();
  }

  initSelectConfig() {
    this.selectConfig = _.defaults({
      data: this.createOptions(),
      allowClear: this.allowClear()
    }, this.config);
  }

  onValue() {
    return this.onChange();
  }

  createOptions() {
    return this.getMetaData().getOptions().map(option => {
      return {id: option.value, text: this.translateService.translateInstant(option.label)};
    });
  }

  allowClear() {
    return !this.attribute.getRestrictions().isMandatory() && !this.getMetaData().hasDefaultValue();
  }

  getMetaData() {
    return this.attribute.getMetaData();
  }
}

