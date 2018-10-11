import {View, Component, Inject, NgElement} from '../../../app/app';
import {TextField} from 'form-builder/textfield/text-field';
import template from './input-text.html!text';

@Component({
  selector: 'seip-input-text',
  properties: {
    'formWrapper': 'form-wrapper',
    'identifier': 'identifier'
  }
})
@View({
  template: template
})
@Inject(NgElement)
class InputText extends TextField {
  constructor($element) {
    super($element);
    this.element = $element;
    this.element.on('keydown', (e) => {
      if (e.keyCode === 13) {
        e.preventDefault();
      }
    });
  }
}
