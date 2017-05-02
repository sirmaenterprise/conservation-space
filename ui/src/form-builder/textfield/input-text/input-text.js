import {View, Component, Inject, NgElement} from '../../../app/app';
import {TextField} from 'form-builder/textfield/text-field';
import template from './input-text.html!text';

@Component({
  selector: 'seip-input-text',
  properties: {
    'fieldViewModel': 'field-view-model',
    'validationModel': 'validation-model',
    'flatFormViewModel': 'flat-form-view-model',
    'form': 'form',
    'validationService': 'validation-service',
    'widgetConfig': 'widget-config',
    'objectId': 'object-id'
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
