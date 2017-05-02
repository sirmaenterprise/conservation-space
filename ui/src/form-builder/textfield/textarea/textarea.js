import {View, Component, Inject, NgElement} from 'app/app';
import 'components/resizabletextarea/resizable-textarea';
import {TextField} from 'form-builder/textfield/text-field';
import template from './textarea.html!text';

@Component({
  selector: 'seip-textarea',
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
class Textarea extends TextField {
  constructor($element) {
    super($element);
  }
}
