import {View, Component, Inject, NgElement} from 'app/app';
import {FormControl} from 'form-builder/form-control';
import template from './header.html!text';

@Component({
  selector: 'seip-header',
  properties: {
    'fieldViewModel': 'field-view-model',
    'validationModel': 'validation-model',
    'flatFormViewModel': 'flat-form-view-model',
    'form': 'form',
    'validationService': 'validation-service',
    'widgetConfig': 'widget-config',
    'formConfig': 'form-config',
    'objectId': 'object-id'
  }
})
@View({
  template: template
})
@Inject(NgElement)
class Header extends FormControl {
  constructor($element) {
    super();
    this.$element = $element;
  }

  ngOnInit(){
    this.setRendered(this.$element, this.fieldViewModel.rendered);
  }
}
