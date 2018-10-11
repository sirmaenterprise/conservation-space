import {View, Component} from 'app/app';

import './model-boolean-attribute.css!css';
import template from './model-boolean-attribute.html!text';

/**
 * Component responsible for rendering single boolean attribute.
 * Attribute model is provided through a component property and
 * should be of type {@link ModelSingleAttribute}.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'model-boolean-attribute',
  properties: {
    'attribute': 'attribute'
  }
})
@View({
  template
})
export class ModelBooleanAttribute {

}