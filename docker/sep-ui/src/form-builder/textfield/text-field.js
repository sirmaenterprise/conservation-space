import {FormControl} from 'form-builder/form-control';
import {DEFAULT_VALUE_PATTERN} from 'form-builder/validation/calculation/calculation';

/**
 * Base class for input-text and textarea fields.
 */
export class TextField extends FormControl {
  constructor($element) {
    super();
    this.$element = $element;
  }

  ngOnInit() {
    this.initElement();
    let initialValue = this.validationModel[this.fieldViewModel.identifier].value;
    if (this.isObjectPropertyValue(initialValue)) {
      this.validationModel[this.fieldViewModel.identifier].value = initialValue.results.join();
    }
    this.editField.val(this.validationModel[this.fieldViewModel.identifier].value);
    this.editField.on('input', () => {
      if (this.isControl(DEFAULT_VALUE_PATTERN)) {
        this.fieldViewModel.editedByUser = true;
      }
      if (this.editField.val()) {
        this.validationModel[this.fieldViewModel.identifier].value = this.editField.val();
      }
    });

    this.editField.on('blur', () => {
      if (this.editField.val().trim().length === 0) {
        this.validationModel[this.fieldViewModel.identifier].value = '';
        this.editField.val(this.validationModel[this.fieldViewModel.identifier].value);
      }
    });

    this.fieldViewModelSubscription = this.fieldViewModel.subscribe('propertyChanged', (propertyChanged) => {
      this.executeCommonPropertyChangedHandler(propertyChanged);
    });
    this.validationModelSubscription = this.validationModel[this.fieldViewModel.identifier].subscribe('propertyChanged', (propertyChanged) => {
      this.executeCommonPropertyChangedHandler(propertyChanged);
    });

    //bind ngmodel to form
    let ngModel = this.$element.controller('ngModel');
    if (this.form && ngModel) {
      ngModel.$name = this.fieldViewModel.identifier;
      this.form.$addControl(ngModel);
    }
  }

  notifyWhenReady() {
    return true;
  }

  isObjectPropertyValue(value) {
    return value instanceof Object && value.results;
  }
}