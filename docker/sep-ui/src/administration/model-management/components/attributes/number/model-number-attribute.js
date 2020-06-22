import {View, Component} from 'app/app';
import {ModelGenericAttribute} from 'administration/model-management/components/attributes/model-generic-attribute';

import template from './model-number-attribute.html!text';

/**
 * Component responsible for rendering number type attribute. Attribute model is provided through a component property
 * and should be of type {@link ModelSingleAttribute}.
 *
 * @author svelikov
 */
@Component({
  selector: 'model-number-attribute',
  properties: {
    'editable': 'editable',
    'attribute': 'attribute'
  },
  events: ['onChange']
})
@View({
  template
})
export class ModelNumberAttribute extends ModelGenericAttribute {

  //@Override
  onModelChange(oldValue) {
    let converted = this.getAsNumber(oldValue);
    return super.onModelChange(converted);
  }

  getAsNumber(value) {
    let parsed = parseInt(value);
    return isNaN(parsed) ? null : parsed;
  }
}