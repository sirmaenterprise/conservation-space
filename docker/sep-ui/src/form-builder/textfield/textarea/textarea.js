import {View, Component, Inject, NgElement} from 'app/app';
import 'components/resizabletextarea/resizable-textarea';
import {TextField} from 'form-builder/textfield/text-field';
import template from './textarea.html!text';

@Component({
  selector: 'seip-textarea',
  properties: {
    'formWrapper': 'form-wrapper',
    'identifier': 'identifier'
  }
})
@View({template})
@Inject(NgElement)
export class Textarea extends TextField {
  constructor($element) {
    super($element);
  }
}
