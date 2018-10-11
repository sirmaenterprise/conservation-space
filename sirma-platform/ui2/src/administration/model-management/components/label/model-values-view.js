import {View, Component} from 'app/app';

import './model-values-view.css!css';
import template from './model-values-view.html!text';

/**
 * Component responsible for rendering all values of a multi
 * valued attribute. Values model is provided through a basic
 * component property and should be of type {@link ModelValue}.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'model-values-view',
  properties: {
    'values': 'values'
  }
})
@View({
  template
})
export class ModelValuesView {
}