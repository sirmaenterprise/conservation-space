import {View, Component, Inject, NgScope} from 'app/app';
import {TranslateService} from 'services/i18n/translate-service';

import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelGenericAttribute} from 'administration/model-management/components/attributes/model-generic-attribute';
import {ModelManagementCodelistService} from 'administration/model-management/services/utility/model-management-codelist-service';
import _ from 'lodash';

import 'administration/model-management/components/attributes/select/model-select-attribute';

import './model-codevalue-attribute.css!css';
import template from './model-codevalue-attribute.html!text';

/**
 * Component responsible for rendering single code value attribute.
 * Attribute model is provided through a component property and
 * should be of type {@link ModelSingleAttribute}. Code values
 * rendered by this component are directly dependant or related
 * to the code list attribute provided by the context component
 * property. On each change of the code list attribute the code
 * value attribute's value is cleared thus triggering the change
 * event automatically.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'model-codevalue-attribute',
  properties: {
    'config': 'config',
    'context': 'context',
    'editable': 'editable',
    'attribute': 'attribute'
  },
  events: ['onChange']
})
@View({template})
@Inject(NgScope, TranslateService, ModelManagementCodelistService)
export class ModelCodeValueAttribute extends ModelGenericAttribute {

  constructor($scope, translateService, modelManagementCodelistService) {
    super();
    this.$scope = $scope;
    this.translateService = translateService;
    this.modelManagementCodelistService = modelManagementCodelistService;
  }

  ngOnInit() {
    this.initSelectConfig();
    this.initSelectOptions();
    this.initCodeListWatcher();
  }

  initCodeListWatcher() {
    // watch the code list number to properly refresh the underlying select data
    this.$scope.$watch(() => this.getCodeListNumber(), (newValue, oldValue) => {
      newValue !== oldValue && this.initSelectOptions();
    });

    // watch for manual changes to the code list attribute to reset the value
    this.subscribe(this.getCodeListAttribute(), ([newValue, oldValue]) => {
      newValue !== oldValue && this.clearAttributeValue();
    });
  }

  initSelectConfig() {
    this.selectConfig = _.defaults({
      allowClear: true
    }, this.config);
  }

  initSelectOptions() {
    this.selectConfig.data = [];
    let code = this.getCodeListNumber();
    code && this.modelManagementCodelistService.getCodeList(code).then(list => {
      this.selectConfig.data = list.values.map(item => this.getSelectItem(item));
    });
  }

  onValue() {
    return this.onChange();
  }

  getCodeListNumber() {
    return this.getCodeListAttribute().getValue().getValue();
  }

  getCodeListAttribute() {
    return this.getAttribute(ModelAttribute.CODELIST_ATTRIBUTE);
  }

  getAttribute(name) {
    return this.context.getAttribute(name);
  }

  getSelectItem(data) {
    return {id: data.value, text: data.label};
  }
}