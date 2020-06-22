import {View, Component, Inject, NgScope} from 'app/app';

import {ModelPrimitives} from 'administration/model-management/model/model-primitives';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelGenericAttribute} from 'administration/model-management/components/attributes/model-generic-attribute';

import 'administration/model-management/components/attributes/string/model-string-attribute';
import 'administration/model-management/components/attributes/bool/model-boolean-attribute';
import 'administration/model-management/components/attributes/source-attr/model-source-attribute';
import 'administration/model-management/components/attributes/codevalue/model-codevalue-attribute';

import './model-value-attribute.css!css';
import template from './model-value-attribute.html!text';

/**
 * Component responsible for rendering value attribute. Attribute model is provided through a component property and
 * should be of type {@link ModelSingleAttribute}.
 *
 * The value attribute type is <b>value</b>. The actual type is string but according to the field type, the way value
 * could be populated may differ. String and number type fields are populated through a source area field.
 * This component allows different data types to be populated in different ways through specific fields like text fields,
 * select menus, checkboxes or composite components for dates.
 *
 * Watchers for typeOption and field's id are used in order to recalculate required editor type and to cache the initial
 * model value.
 *
 * @author svelikov
 */
@Component({
  selector: 'model-value-attribute',
  properties: {
    'config': 'config',
    'context': 'context',
    'editable': 'editable',
    'attribute': 'attribute'
  },
  events: ['onChange']
})
@View({template})
@Inject(NgScope)
export class ModelValueAttribute extends ModelGenericAttribute {

  constructor($scope) {
    super();
    this.$scope = $scope;
  }

  ngOnInit() {
    this.computeEditorType();
    this.initContextWatcher();
    this.initTypeOptionWatcher();
  }

  onValue() {
    return this.onChange();
  }

  initContextWatcher() {
    this.$scope.$watch(() => this.context, (newValue, oldValue) => {
      // re-compute editor type when context has changed
      newValue !== oldValue && this.computeEditorType();
    });
  }

  initTypeOptionWatcher() {
    // watch the type option value to properly refresh the underlying view type
    this.$scope.$watch(() => this.getTypeOptionValue(), (newValue, oldValue) => {
      newValue !== oldValue && this.computeEditorType();
    });

    // watch only for manual changes to refresh the value
    this.subscribe(this.getTypeOptionAttribute(), ([newValue, oldValue]) => {
      newValue !== oldValue && this.clearAttributeValue();
    });
  }

  computeEditorType() {
    this.valueEditorType = ModelValueAttribute.EDITOR_TYPE[this.getTypeOptionValue()];
  }

  getTypeOptionValue() {
    return this.getTypeOptionAttribute().getValue().getValue();
  }

  getTypeOptionAttribute() {
    return this.getAttribute(ModelAttribute.TYPE_OPTION_ATTRIBUTE);
  }

  getAttribute(name) {
    return this.context.getAttribute(name);
  }
}

ModelValueAttribute.SOURCE_TYPE = 'source';
ModelValueAttribute.BOOLEAN_TYPE = 'boolean';
ModelValueAttribute.CODEVALUE_TYPE = 'codevalue';

ModelValueAttribute.EDITOR_TYPE = {
  [ModelPrimitives.ALPHA_NUMERIC_TYPE]: ModelValueAttribute.SOURCE_TYPE,
  [ModelPrimitives.ALPHA_NUMERIC_FIXED_TYPE]: ModelValueAttribute.SOURCE_TYPE,
  [ModelPrimitives.ALPHA_NUMERIC_WITH_CONSTRAINTS_TYPE]: ModelValueAttribute.SOURCE_TYPE,
  [ModelPrimitives.NUMERIC_TYPE]: ModelValueAttribute.SOURCE_TYPE,
  [ModelPrimitives.NUMERIC_FIXED_TYPE]: ModelValueAttribute.SOURCE_TYPE,
  [ModelPrimitives.FLOATING_POINT_TYPE]: ModelValueAttribute.SOURCE_TYPE,
  [ModelPrimitives.FLOATING_POINT_FIXED_TYPE]: ModelValueAttribute.SOURCE_TYPE,
  [ModelPrimitives.DATE_TYPE]: ModelValueAttribute.SOURCE_TYPE,
  [ModelPrimitives.DATETIME_TYPE]: ModelValueAttribute.SOURCE_TYPE,
  [ModelPrimitives.BOOLEAN]: ModelValueAttribute.BOOLEAN_TYPE,
  [ModelPrimitives.CODELIST]: ModelValueAttribute.CODEVALUE_TYPE,
  [ModelPrimitives.URI]: ModelValueAttribute.SOURCE_TYPE
};