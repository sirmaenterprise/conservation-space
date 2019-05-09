import {View, Component} from 'app/app';
import {ModelGenericControl} from 'administration/model-management/components/field/control/model-generic-control';

import 'administration/model-management/components/attributes/string/model-string-attribute';
import 'administration/model-management/components/attributes/source-attr/model-source-attribute';
import 'components/hint/label-hint';

import './model-picker-control.css!css';
import template from './model-picker-control.html!text';

const PARAM_RANGE = 'range';
const PARAM_RESTRICTIONS = 'restrictions';

/**
 * Component responsible for rendering specific view for the PICKER.
 * Control model is provided through a component property and should be of type {@link ModelControl}.
 *
 * @author svelikov
 */
@Component({
  selector: 'model-picker-control',
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
export class ModelPickerControl extends ModelGenericControl {

  getRangeValueAttribute() {
    return this.getParamValue(PARAM_RANGE);
  }

  onRangeChange() {
    return this.onChange({attribute: this.getParamValue(PARAM_RANGE)});
  }

  getRestrictionsValueAttribute() {
    return this.getParamValue(PARAM_RESTRICTIONS);
  }

  onRestrictionsChange() {
    return this.onChange({attribute: this.getParamValue(PARAM_RESTRICTIONS)});
  }
}