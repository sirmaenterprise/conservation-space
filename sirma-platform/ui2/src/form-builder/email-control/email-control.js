import {View, Component, Inject, NgElement} from 'app/app';
import {TextField} from 'form-builder/textfield/text-field';
import template from './email-control.html!text';

@Component({
  selector: 'seip-email-control',
  properties: {
    'formWrapper': 'form-wrapper',
    'identifier': 'identifier'
  }
})
@View({
  template: template
})
@Inject(NgElement)
export class EmailControl extends TextField {
  constructor($element) {
    super($element);
  }
}
