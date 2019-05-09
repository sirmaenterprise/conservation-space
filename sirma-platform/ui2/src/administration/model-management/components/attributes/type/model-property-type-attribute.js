import {View, Component, Inject, NgScope} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {TranslateService} from 'services/i18n/translate-service';
import {ModelPropertyMetaData} from 'administration/model-management/meta/model-property-meta';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelManagementModelsService} from 'administration/model-management/services/utility/model-management-models-service';
import {ModelGenericAttribute} from 'administration/model-management/components/attributes/model-generic-attribute';

import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';
import _ from 'lodash';

import 'administration/model-management/components/attributes/select/model-select-attribute';

import template from './model-property-type-attribute.html!text';

/**
 * Component rendering a propertyType dropdown.
 * Attribute model is provided through a component property and should be of type {@link ModelSingleAttribute}.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'model-property-type-attribute',
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
@Inject(NgScope, PromiseAdapter, TranslateService, ModelManagementModelsService)
export class ModelPropertyTypeAttribute extends ModelGenericAttribute {

  constructor($scope, promiseAdapter, translateService, modelManagementModelsService) {
    super();
    this.$scope = $scope;
    this.promiseAdapter = promiseAdapter;
    this.translateService = translateService;
    this.modelManagementModelsService = modelManagementModelsService;
  }

  ngOnInit() {
    this.initSelectConfig();
    this.initSelectOptions();
    this.initRangeWatcher();
  }

  onValue() {
    return this.onChange();
  }

  initSelectConfig() {
    this.selectConfig = _.defaults({}, this.config);
  }

  initRangeWatcher() {
    this.$scope.$watch(() => this.getRangeValue(), (newValue, oldValue) => {
      newValue !== oldValue && this.initSelectOptions();
    });
  }

  initSelectOptions() {
    let range = this.getRangeValue();
    let options = this.createOptions();

    // when a range is present filter the semantic types based off of it
    if (!ModelManagementUtility.isAttributeEmpty(this.getRangeAttribute())) {
      return this.promiseAdapter.all(options.map(o => {
        // map a promise call for each of the available property types
        return this.modelManagementModelsService.getTypes(this.getType(o.id));
      })).then(types => {
        let found = this.findType(range, types);
        // resolve type from the array of options using the target index
        this.selectConfig.data = found > -1 ? [options[found]] : options;
      });
    }

    // no range is present to filter
    this.selectConfig.data = options;
  }

  findType(range, types) {
    for (let i = 0; i < types.length; ++i) {
      if (_.find(types[i], t => t.id === range)) {
        return i;
      }
    }
    return -1;
  }

  createOptions() {
    return this.getOptions().map(option => {
      return {id: option.value, text: this.translateService.translateInstant(option.label)};
    });
  }

  getType(type) {
    return ModelPropertyTypeAttribute.TYPE_MAPPING[type];
  }

  getRangeValue() {
    return this.getRangeAttribute().getValue().getValue();
  }

  getRangeAttribute() {
    return this.context.getAttribute(ModelAttribute.RANGE_ATTRIBUTE);
  }

  getOptions() {
    return this.attribute.getMetaData().getOptions();
  }
}

ModelPropertyTypeAttribute.TYPE_MAPPING = {
  [ModelPropertyMetaData.DATA_PROPERTY]: 'DATA_TYPE',
  [ModelPropertyMetaData.OBJECT_PROPERTY]: 'OBJECT_TYPE'
};

