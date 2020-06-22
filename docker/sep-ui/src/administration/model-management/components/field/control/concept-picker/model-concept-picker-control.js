import {View, Component} from 'app/app';
import {ModelGenericControl} from 'administration/model-management/components/field/control/model-generic-control';

import 'administration/model-management/components/attributes/string/model-string-attribute';
import 'components/hint/label-hint';

import './model-concept-picker-control.css!css';
import template from './model-concept-picker-control.html!text';

const PARAM_SCHEME = 'scheme';
const PARAM_BROADER = 'broader';

/**
 * Component responsible for rendering specific view for the CONCEPT_PICKER control.
 * Control model is provided through a component property and should be of type {@link ModelControl}.
 *
 * @author svelikov
 */
@Component({
  selector: 'model-concept-picker-control',
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
export class ModelConceptPickerControl extends ModelGenericControl {

  getSchemeValueAttribute() {
    return this.getParamValue(PARAM_SCHEME);
  }

  onSchemeChange() {
    return this.onChange({attribute: this.getParamValue(PARAM_SCHEME)});
  }

  getBroaderValueAttribute() {
    return this.getParamValue(PARAM_BROADER);
  }

  onBroaderChange() {
    return this.onChange({attribute: this.getParamValue(PARAM_BROADER)});
  }

}