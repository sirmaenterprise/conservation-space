import {View, Component} from 'app/app';
import {ModelAttributeTypes} from 'administration/model-management/model/attributes/model-attribute-types';
import {ModelGenericAttribute} from 'administration/model-management/components/attributes/model-generic-attribute';

import _ from 'lodash';

import './model-boolean-attribute.css!css';
import template from './model-boolean-attribute.html!text';

const TRUE = 'true';
const FALSE = 'false';

/**
 * Component responsible for rendering single boolean attribute.
 * Attribute model is provided through a component property and
 * should be of type {@link ModelSingleAttribute}. The boolean
 * attribute implementation is such that it supports both boolean
 * primitives and boolean values represented as strings such as
 * 'true', 'false' and empty string which is treated as false.
 * The implementation depends on the provided attribute value type
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'model-boolean-attribute',
  properties: {
    'editable': 'editable',
    'attribute': 'attribute'
  },
  events: ['onChange']
})
@View({
  template
})
export class ModelBooleanAttribute extends ModelGenericAttribute {

  ngOnInit() {
    this.isBoolean = _.isBoolean(this.getValue());
  }

  //@Override
  onModelChange(oldValue) {
    if (this.isBooleanPrimitive()) {
      oldValue = this.getAsBooleanPrimitive(oldValue);
    } else {
      oldValue = this.getAsBooleanString(oldValue);
    }
    return super.onModelChange(oldValue);
  }

  getAsBooleanPrimitive(value) {
    return value === TRUE;
  }

  getAsBooleanString(value) {
    let model = this.attribute.getValue();
    let type = this.attribute.getType();

    let current = model.getValue().toString();
    let base = ModelAttributeTypes.getDefaultValue(type);
    let change = current === FALSE && !model.getOldValue();

    model.setValue(change ? base : current);
    return value;
  }

  isBooleanPrimitive() {
    return this.isBoolean;
  }
}

