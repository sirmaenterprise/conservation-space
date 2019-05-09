import {View, Component} from 'app/app';
import {ModelGenericAttribute} from 'administration/model-management/components/attributes/model-generic-attribute';
import 'components/sourcearea/sourcearea';
import _ from 'lodash';
import './model-source-attribute.css!css';
import template from './model-source-attribute.html!text';

/**
 * Component responsible for rendering attribute which contains structured string value like source code, regex, expressions.
 * Attribute model is provided through a component property and should be of type {@link ModelSingleAttribute}.
 *
 * @author svelikov
 */
@Component({
  selector: 'model-source-attribute',
  properties: {
    'editable': 'editable',
    'attribute': 'attribute',
    'config': 'config'
  },
  events: ['onChange']
})
@View({template})
export class ModelSourceAttribute extends ModelGenericAttribute {

  ngOnInit() {
    this.sourceareaConfig = _.defaults(this.config, {
      lineNumbers: false,
      readOnly: !this.editable
    });
  }
}