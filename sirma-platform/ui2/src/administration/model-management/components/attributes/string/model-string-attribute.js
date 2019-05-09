import {View, Component} from 'app/app';
import {ModelGenericAttribute} from 'administration/model-management/components/attributes/model-generic-attribute';

import template from './model-string-attribute.html!text';

/**
 * Component responsible for rendering a plain string attribute.
 * Attribute model is provided through a component property and
 * should be of type {@link ModelSingleAttribute}.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'model-string-attribute',
  properties: {
    'editable': 'editable',
    'attribute': 'attribute'
  },
  events: ['onChange']
})
@View({
  template
})
export class ModelStringAttribute extends ModelGenericAttribute {
}