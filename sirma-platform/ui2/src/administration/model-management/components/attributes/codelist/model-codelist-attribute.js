import {View, Component, Inject, NgScope} from 'app/app';
import {TranslateService} from 'services/i18n/translate-service';

import {ModelGenericAttribute} from 'administration/model-management/components/attributes/model-generic-attribute';
import {ModelManagementCodelistService} from 'administration/model-management/services/utility/model-management-codelist-service';

import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelPrimitives} from 'administration/model-management/model/model-primitives';
import _ from 'lodash';

import 'administration/model-management/components/attributes/select/model-select-attribute';

import template from './model-codelist-attribute.html!text';

/**
 * Component rendering a drop down with all code lists in the system
 * Attribute model is provided through a component property and should
 * be of type {@link ModelSingleAttribute}.
 *
 * @author Stella Djulgerova
 */
@Component({
  selector: 'model-codelist-attribute',
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
@Inject(NgScope, TranslateService, ModelManagementCodelistService)
export class ModelCodelistAttribute extends ModelGenericAttribute {

  constructor($scope, translateService, modelManagementCodelistService) {
    super();
    this.$scope = $scope;
    this.translateService = translateService;
    this.modelManagementCodelistService = modelManagementCodelistService;
  }

  ngOnInit() {
    this.initSelectConfig();
    this.initSelectOptions();
    this.initTypeOptionWatcher();
  }

  onValue() {
    return this.onChange();
  }

  initSelectConfig() {
    this.selectConfig = _.defaults({
      allowClear: true
    }, this.config);
  }

  initSelectOptions() {
    this.modelManagementCodelistService.getCodeLists().then((response) => {
      this.selectConfig.data = response.map(codeList => this.transformCodeList(codeList));
    });
  }

  initTypeOptionWatcher() {
    this.subscribe(this.getTypeOptionAttribute(), (typeOption) => {
      let oldValue = this.attribute.getValue().getValue();
      if (typeOption !== ModelPrimitives.CODELIST && oldValue) {
        this.clearAttributeValue();
      }
    });
  }

  transformCodeList(codeList) {
    return {
      id: codeList.value,
      text: `${codeList.value} - ${codeList.label}`
    };
  }

  getTypeOptionAttribute() {
    return this.context.getAttribute(ModelAttribute.TYPE_OPTION_ATTRIBUTE);
  }
}