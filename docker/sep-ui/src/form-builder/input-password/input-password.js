import {View, Component, Inject, NgElement} from 'app/app';
import {FormControl} from 'form-builder/form-control';
import template from './input-password.html!text';

@Component({
  selector: 'seip-input-password',
  properties: {
    'formWrapper': 'form-wrapper',
    'identifier': 'identifier'
  }
})
@View({template})
@Inject(NgElement)
export class InputPassword extends FormControl {
  constructor($element) {
    super();
    this.$element = $element;
  }

  ngOnInit() {
    this.validationModelSubscription = this.validationModel[this.fieldViewModel.identifier].subscribe('propertyChanged', (propertyChanged) => {
      this.executeCommonPropertyChangedHandler(propertyChanged);
    });
  }

  makeEditable() {
    // the password fields are readonly in order to prevent chrome password autofill, since chrome ignores autocomplete
    // attribute and suggests passwords anyway
    // thats why we remove the readonly attribute here
    this.$element.find('input').removeAttr('readonly');
  }
}
