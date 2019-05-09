import {View, Component, Inject, NgScope} from 'app/app';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelPropertyMetaData} from 'administration/model-management/meta/model-property-meta';
import {ModelManagementModelsService} from 'administration/model-management/services/utility/model-management-models-service';
import {ModelGenericAttribute} from 'administration/model-management/components/attributes/model-generic-attribute';
import _ from 'lodash';

import 'administration/model-management/components/attributes/select/model-select-attribute';

import template from './model-range-attribute.html!text';

/**
 * Component responsible for resolving and rendering a domain attribute. Domain attribute is a drop down
 * attribute which takes care of visualizing all models which belong to the domain of the owner of the
 * provided attribute as component property to this view.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'model-range-attribute',
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
@Inject(NgScope, ModelManagementModelsService)
export class ModelRangeAttribute extends ModelGenericAttribute {

  constructor($scope, modelManagementModelsService) {
    super();
    this.$scope = $scope;
    this.modelManagementModelsService = modelManagementModelsService;
  }

  ngOnInit() {
    this.initSelectConfig();
    this.initSelectOptions();
    this.initContextWatcher();
  }

  onValue() {
    return this.onChange();
  }

  initContextWatcher() {
    this.$scope.$watch(() => this.getTypeValue(), (newValue, oldValue) => {
      newValue !== oldValue && this.initSelectOptions();
    });
  }

  initSelectConfig() {
    this.selectConfig = _.defaults({
      allowClear: true,
      appendMissing: true
    }, this.config);
  }

  initSelectOptions() {
    let type = this.getType(this.getTypeValue());
    this.modelManagementModelsService.getTypes(type).then(types => {
      // reload data select config options based on the type
      this.selectConfig.data = this.getSelectData(types);
    });
  }

  getSelectData(types) {
    return types.map(this.getItem.bind(this));
  }

  getItem(item) {
    return {id: item.id, text: item.label};
  }

  getType(type) {
    return ModelRangeAttribute.TYPE_MAPPING[type];
  }

  getTypeValue() {
    return this.getTypeAttribute().getValue().getValue();
  }

  getTypeAttribute() {
    return this.context.getAttribute(ModelAttribute.PROPERTY_TYPE);
  }
}

ModelRangeAttribute.TYPE_MAPPING = {
  [ModelPropertyMetaData.DATA_PROPERTY]: 'DATA_TYPE',
  [ModelPropertyMetaData.OBJECT_PROPERTY]: 'OBJECT_TYPE'
};