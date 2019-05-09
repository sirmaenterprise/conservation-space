import {View, Component} from 'app/app';
import {ModelGenericControl} from 'administration/model-management/components/field/control/model-generic-control';

import 'administration/model-management/components/attributes/source-attr/model-source-attribute';
import 'components/hint/label-hint';

import './model-default-value-pattern-control.css!css';
import template from './model-default-value-pattern-control.html!text';

const PARAM_TEMPLATE = 'template';

/**
 * Component responsible for rendering specific view for the DEFAULT_VALUE_PATTERN control.
 * Control model is provided through a component property and should be of type {@link ModelControl}.
 *
 * @author svelikov
 */
@Component({
  selector: 'model-default-value-pattern-control',
  properties: {
    'control': 'control',
    'context': 'context',
    'editable': 'editable'
  },
  events: ['onChange']
})
@View({
  template
})
export class ModelDefaultValuePatternControl extends ModelGenericControl {

  getTemplateValueAttribute() {
    return this.getParamValue(PARAM_TEMPLATE);
  }

  onTemplateChange() {
    return this.onChange({attribute: this.getParamValue(PARAM_TEMPLATE)});
  }

}