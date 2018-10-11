import {Component, View} from 'app/app';
import {PREVIEW} from 'administration/code-lists/manage/code-manage-modes';

import './code-descriptions.css!css';
import template from './code-descriptions.html!text';

/**
 * Component for visualizing the descriptions for given code list.
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'code-descriptions',
  properties: {
    'mode': 'mode',
    'onChange': 'onChange',
    'descriptions': 'descriptions'
  }
})
@View({
  template
})
export class CodeDescriptions {

  onModelChange(value) {
    value.isModified = true;
    this.onChange();
  }

  isPreviewMode() {
    return this.mode === PREVIEW;
  }

  isValueFieldInvalid(value, field) {
    // If there is no validation model then the field value hasn't been changed
    return !!value.validationModel && value.validationModel[field] && !value.validationModel[field].valid;
  }
}
