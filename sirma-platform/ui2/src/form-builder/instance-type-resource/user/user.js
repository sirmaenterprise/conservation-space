import {View, Component, Inject, NgElement} from 'app/app';
import {InstanceTypeResource} from 'form-builder/instance-type-resource/instance-type-resource';
import {InstanceSelector} from 'components/instance-selector/instance-selector';
import template from './user.html!text';

@Component({
  selector: 'seip-user',
  properties: {
    'formWrapper': 'form-wrapper',
    'identifier': 'identifier'
  }
})
@View({
  template: template
})
@Inject(NgElement)
export class User extends InstanceTypeResource {
  constructor($element) {
    super($element);
  }

  ngOnInit() {
    this.instanceSelector = this.$element.find('.form-field');
    this.mandatoryMark = this.$element.find('.mandatory-mark');

    this.setRendered(this.$element, this.fieldViewModel.rendered);
    this.renderMark(this.mandatoryMark, this.renderMandatoryMark());

    this.fieldViewModelSubscription = this.fieldViewModel.subscribe('propertyChanged', (propertyChanged) => {
      this.executeCommonPropertyChangedHandler(propertyChanged);
    });
  }
}
