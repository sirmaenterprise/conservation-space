import {View, Component} from 'app/app';

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
    'attribute': 'attribute'
  }
})
@View({
  template
})
export class ModelStringAttribute {
}