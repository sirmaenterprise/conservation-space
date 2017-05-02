import {View, Component, Inject, NgElement} from 'app/app';
import {FormControl} from 'form-builder/form-control';
import template from './input-password.html!text';

@Component({
  selector: 'seip-input-password',
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
class InputPassword extends FormControl {
  constructor($element) {
    super();
    this.$element = $element;
  }

  ngOnInit() {
    this.validationModelSubscription = this.validationModel[this.fieldViewModel.identifier].subscribe('propertyChanged', (propertyChanged) => {
      this.executeCommonPropertyChangedHandler(propertyChanged);
    });
  }
}
