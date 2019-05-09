import {View, Component, Inject, NgElement} from 'app/app';
import template from './validation-message.html!text';

@Component({
  selector: 'seip-validation-message',
  properties: {
    'validationModel': 'validationModel',
    'fieldCtrl': 'fieldCtrl',
    'viewModel': 'viewModel'
  }
})
@View({template})
@Inject(NgElement)
export class ValidationMessage {
  constructor($element) {
    this.$element = $element;
  }

  ngOnInit() {
    this.baseElement = this.$element.find('.message');
    this.initValidationMessages();

    this.validationModelSubscription = this.validationModel.subscribe('propertyChanged', (propertyChanged) => {
      if (Object.keys(propertyChanged)[0] === 'messages') {
        this.initValidationMessages();
      }
    });

    this.viewModelSubscription = this.viewModel.subscribe('propertyChanged', (propertyChanged) => {
      if (Object.keys(propertyChanged)[0] === 'preview') {
        this.initValidationMessages();
      }
    });
  }

  initValidationMessages() {
    let validationMessages = '';
    if (this.validationModel.messages) {
      this.validationModel.messages.forEach((message) => {
        validationMessages += (`<span id="${message.id}" class="${message.level}">${message.message}</span>`);
      });
    }
    this.baseElement.html(validationMessages);
  }

  ngOnDestroy() {
    this.baseElement.empty().remove();
    $(this.$element).empty().remove();

    if (this.viewModelSubscription) {
      this.viewModelSubscription.unsubscribe();
    }
    if (this.validationModelSubscription) {
      this.validationModelSubscription.unsubscribe();
    }

  }
}
