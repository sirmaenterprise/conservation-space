import {View, Component} from 'app/app';

import template from './model-validation-messages.html!text';

@Component({
  selector: 'model-validation-messages',
  properties: {
    'validation': 'validation'
  }
})
@View({
  template
})
export class ModelValidationMessages {

  getErrors() {
    return this.validation.getErrors();
  }
}