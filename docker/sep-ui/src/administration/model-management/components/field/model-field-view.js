import {View, Component, Inject, NgScope} from 'app/app';
import {ValidationService} from 'form-builder/validation/validation-service';

import {ModelPrimitives} from 'administration/model-management/model/model-primitives';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelManagementCodelistService} from 'administration/model-management/services/utility/model-management-codelist-service';

import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';
import _ from 'lodash';

import 'administration/model-management/components/controls/select/model-select';

import 'components/datetimepicker/datetimepicker';
import 'components/select/select';
import 'components/hint/label-hint';

import './model-field-view.css!css';
import template from './model-field-view.html!text';

const TEXT = 'text';
const PICKER = 'picker';
const SELECT = 'select';
const DATETIME = 'date';
const BOOLEAN = 'boolean';

const VALUE_ATTRIBUTE = ModelAttribute.VALUE_ATTRIBUTE;
const TOOLTIP_ATTRIBUTE = ModelAttribute.TOOLTIP_ATTRIBUTE;
const CODELIST_ATTRIBUTE = ModelAttribute.CODELIST_ATTRIBUTE;
const DISPLAY_ATTRIBUTE = ModelAttribute.DISPLAY_ATTRIBUTE;
const MANDATORY_ATTRIBUTE = ModelAttribute.MANDATORY_ATTRIBUTE;
const MULTIVALUE_ATTRIBUTE = ModelAttribute.MULTIVALUE_ATTRIBUTE;
const DESCRIPTION_ATTRIBUTE = ModelAttribute.DESCRIPTION_ATTRIBUTE;
const TYPE_OPTION_ATTRIBUTE = ModelAttribute.TYPE_OPTION_ATTRIBUTE;

const EDITABLE = ValidationService.DISPLAY_TYPE_EDITABLE;

/**
 * A component in charge of displaying a single model field.
 * The provided model is supplied through a component property.
 * The proved region should be of type {@link ModelField}
 * or any types extending off of it.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'seip-model-field-view',
  properties: {
    'model': 'model',
    'context': 'context'
  },
  events: ['onModelSelected']
})
@View({
  template
})
@Inject(NgScope, ModelManagementCodelistService)
export class ModelFieldView {

  constructor($scope, modelManagementCodelistService) {
    this.$scope = $scope;
    this.modelManagementCodelistService = modelManagementCodelistService;
  }

  ngOnInit() {
    this.initCodeListWatcher();
    this.initializeCodeListControl();
    this.initDefaultValueWatcher();
    this.initDefaultValue();
  }

  initCodeListWatcher() {
    this.$scope.$watch(() => this.getCodeList(), (newValue, oldValue) => {
      // re-initialize the code list data only when it changes
      newValue !== oldValue && this.initializeCodeListControl();
    });
  }

  initializeCodeListControl() {
    if (this.getType() !== SELECT) {
      return;
    }

    // fetch the code list provided as attribute in the current field model
    let code = this.getCodeList();
    code && this.modelManagementCodelistService.getCodeList(code).then(list => {
      this.initializeSelectConfig();

      // configure select based on the additional field attributes
      this.selectConfig.multiple = this.getAttributeValue(MULTIVALUE_ATTRIBUTE);
      this.selectConfig.data = list.values.map(item => this.getSelectItem(item));
      this.value = this.resolveValueFromCodeList();
    });
  }

  initDefaultValueWatcher() {
    this.$scope.$watch(() => this.getValue(), (newValue, oldValue) => {
      // Update field value when default value is changed
      newValue !== oldValue && this.initDefaultValue();
    });
  }

  initDefaultValue() {
    this.value = this.getDisplayValue();
  }

  getDisplayValue() {
    let value = this.getValue();
    if (this.getType() === SELECT && this.selectConfig) {
      value = this.resolveValueFromCodeList();
    } else if (this.getType() === BOOLEAN) {
      value = this.getValue() === 'true';
    }
    return value;
  }

  resolveValueFromCodeList() {
    // resolve value from list of existing code values inside the code list
    let value = this.getValue();
    let found = _.find(this.selectConfig.data, item => item.id === value);
    let displayValue = found ? found.id : '';
    if (this.isReadOnly()) {
      displayValue = found ? found.text : '';
    }
    return displayValue;
  }

  initializeSelectConfig() {
    // skip reinitializing it
    if (this.selectConfig) {
      return;
    }
    // prepare base config
    this.selectConfig = {
      disabled: false,
      selectOnClose: false,
      reloadOnDataChange: true
    };
  }

  selectModel() {
    this.onModelSelected && this.onModelSelected({model: this.model});
  }

  getType() {
    return ModelFieldView.TYPE_CONVERTER[this.getOptionType()];
  }

  getTooltip() {
    let tooltip = this.getAttributeValue(TOOLTIP_ATTRIBUTE);
    if (!tooltip || !tooltip.length) {
      let property = this.model.getProperty();
      tooltip = this.getAttributeValue(DESCRIPTION_ATTRIBUTE, property);
    }
    return tooltip;
  }

  getValue() {
    return this.getAttributeValue(VALUE_ATTRIBUTE);
  }

  getOptionType() {
    return this.getAttributeValue(TYPE_OPTION_ATTRIBUTE);
  }

  getMandatory() {
    return this.getAttributeValue(MANDATORY_ATTRIBUTE);
  }

  getCodeList() {
    return this.getAttributeValue(CODELIST_ATTRIBUTE);
  }

  getAttributeValue(attribute, model = this.model) {
    let attr = model && model.getAttribute(attribute);
    return attr && attr.getValue().getValue();
  }

  getSelectItem(data) {
    return {id: data.value, text: data.label};
  }

  isParentVisible() {
    return ModelManagementUtility.isInherited(this.model, this.context);
  }

  isReadOnly() {
    return !this.isEditable();
  }

  isEditable() {
    return this.getAttributeValue(DISPLAY_ATTRIBUTE) === EDITABLE;
  }
}

ModelFieldView.TYPE_CONVERTER = {
  [ModelPrimitives.ALPHA_NUMERIC_TYPE]: TEXT,
  [ModelPrimitives.ALPHA_NUMERIC_FIXED_TYPE]: TEXT,
  [ModelPrimitives.ALPHA_NUMERIC_WITH_CONSTRAINTS_TYPE]: TEXT,
  [ModelPrimitives.FLOATING_POINT_TYPE]: TEXT,
  [ModelPrimitives.FLOATING_POINT_FIXED_TYPE]: TEXT,
  [ModelPrimitives.NUMERIC_TYPE]: TEXT,
  [ModelPrimitives.NUMERIC_FIXED_TYPE]: TEXT,
  [ModelPrimitives.DATE_TYPE]: DATETIME,
  [ModelPrimitives.DATETIME_TYPE]: DATETIME,
  [ModelPrimitives.BOOLEAN]: BOOLEAN,
  [ModelPrimitives.CODELIST]: SELECT,
  [ModelPrimitives.URI]: PICKER
};
