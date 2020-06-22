import {Component, View} from 'app/app';
import template from './code-validation-messages.html!text';

@Component({
  selector: 'code-validation-messages',
  properties: {
    'validationField': 'validation-field'
  }
})
@View({
  template
})
export class CodeValidationMessages {
  // Just a view component, no logic
}