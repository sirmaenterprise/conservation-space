import {View, Component, Inject, NgScope} from 'app/app';
import {ModelGenericAttribute} from 'administration/model-management/components/attributes/model-generic-attribute';

import {ModelPrimitives} from 'administration/model-management/model/model-primitives';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';

import './model-constraint-attribute.css!css';
import template from './model-constraint-attribute.html!text';

/**
 * Component responsible for rendering display type attribute constraints.
 * Attribute model is provided through a component property and should be of type {@link ModelSingleAttribute}.
 *
 * @author Stela Djulgerova
 */
@Component({
  selector: 'model-constraint-attribute',
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
@Inject(NgScope)
export class ModelConstraintAttribute extends ModelGenericAttribute {

  constructor($scope) {
    super();
    this.$scope = $scope;
  }

  ngOnInit() {
    this.initComponentView();
    this.initTypeWatcher();
    this.initTypeOptionWatcher();
  }

  initComponentView() {
    this.type = this.getTypeOptionValue();
    this.clearConstraints();
    this.updateConstraints();
  }

  initTypeWatcher() {
    this.$scope.$watch(() => this.getTypeValue(), (newValue, oldValue) => {
      // re-compute the comp view after value has changed
      newValue !== oldValue && this.initComponentView();
    });
  }

  initTypeOptionWatcher() {
    this.$scope.$watch(() => this.getTypeOptionValue(), (newValue, oldValue) => {
      // re-compute the comp view after context has changed
      newValue !== oldValue && this.initComponentView();
    });

    this.subscribe(this.getTypeOptionAttribute(), ([newValue]) => {
      this.type = newValue;
      let value = this.getType();

      this.clearConstraints();
      this.updateValue(value);
    });
  }

  updateConstraints() {
    let attributeValue = this.attribute.getValue().getValue();
    if (this.isFloatingPoint()) {
      let values = attributeValue.substring(ModelConstraintAttribute.TYPE_MAP[this.type].length).split(',');
      this.floatingPointLength = values[0];
      this.afterFloatingPointLength = values[1];
    }
    if (this.isAlphaNumeric()) {
      this.alphaNumericLength = attributeValue.substring(ModelConstraintAttribute.TYPE_MAP[this.type].length);
    }
  }

  onAlphaNumericLengthChange(value) {
    this.updateValue(this.concatenateValue(value, null));
  }

  onFloatingPointLengthChange(value) {
    this.updateValue(this.concatenateValue(value, this.afterFloatingPointLength));
  }

  onAfterFloatingPointLengthChange(value) {
    this.updateValue(this.concatenateValue(this.floatingPointLength, value));
  }

  concatenateValue(fullLength = '', afterFloatingPointLength = '') {
    let value = `${ModelConstraintAttribute.TYPE_MAP[this.type]}${fullLength}`;
    if (afterFloatingPointLength && afterFloatingPointLength.length) {
      value += `${ModelConstraintAttribute.SEPARATOR}${afterFloatingPointLength}`;
    }
    return value;
  }

  updateValue(newValue) {
    let oldValue = this.attribute.getValue().getValue();
    this.attribute.getValue().setValue(newValue);
    this.onModelChange(oldValue);
  }

  clearConstraints() {
    delete this.floatingPointLength;
    delete this.afterFloatingPointLength;
    delete this.alphaNumericLength;
  }

  isAlphaNumeric() {
    return this.type === ModelPrimitives.ALPHA_NUMERIC_WITH_CONSTRAINTS_TYPE || this.type === ModelPrimitives.ALPHA_NUMERIC_FIXED_TYPE ||
      this.type === ModelPrimitives.NUMERIC_TYPE || this.type === ModelPrimitives.NUMERIC_FIXED_TYPE;
  }

  isFloatingPoint() {
    return this.type === ModelPrimitives.FLOATING_POINT_TYPE || this.type === ModelPrimitives.FLOATING_POINT_FIXED_TYPE;
  }

  getType() {
    return ModelConstraintAttribute.TYPE_MAP[this.type];
  }

  getTypeOptionValue() {
    return this.getTypeOptionAttribute().getValue().getValue();
  }

  getTypeOptionAttribute() {
    return this.context.getAttribute(ModelAttribute.TYPE_OPTION_ATTRIBUTE);
  }

  getTypeValue() {
    return this.getTypeAttribute().getValue().getValue();
  }

  getTypeAttribute() {
    return this.context.getAttribute(ModelAttribute.TYPE_ATTRIBUTE);
  }
}

ModelConstraintAttribute.SEPARATOR = ',';
ModelConstraintAttribute.TYPE_MAP = {
  [ModelPrimitives.ALPHA_NUMERIC_TYPE]: 'ANY',
  [ModelPrimitives.ALPHA_NUMERIC_FIXED_TYPE]: 'an',
  [ModelPrimitives.ALPHA_NUMERIC_WITH_CONSTRAINTS_TYPE]: 'an..',
  [ModelPrimitives.FLOATING_POINT_TYPE]: 'n..',
  [ModelPrimitives.FLOATING_POINT_FIXED_TYPE]: 'n',
  [ModelPrimitives.NUMERIC_TYPE]: 'n..',
  [ModelPrimitives.NUMERIC_FIXED_TYPE]: 'n',
  [ModelPrimitives.DATE_TYPE]: 'date',
  [ModelPrimitives.DATETIME_TYPE]: 'datetime',
  [ModelPrimitives.BOOLEAN]: 'boolean',
  [ModelPrimitives.CODELIST]: 'ANY',
  [ModelPrimitives.URI]: 'uri'
};