import {View, Component, Inject, NgElement} from 'app/app';
import {FormControl} from 'form-builder/form-control';
import {TranslateService} from 'services/i18n/translate-service';
import template from 'form-builder/checkbox/checkbox.html!text';

@Component({
  selector: 'seip-checkbox',
  properties: {
    'formWrapper': 'form-wrapper',
    'identifier': 'identifier'
  }
})
@View({
  template: template
})
@Inject(TranslateService, NgElement)
class Checkbox extends FormControl {
  constructor(translateService, $element) {
    super();
    this.$element = $element;
    this.CHECKED = translateService.translateInstant('checkbox.checked');
    this.UNCHECKED = translateService.translateInstant('checkbox.unchecked');
  }

  ngOnInit() {
    //checkboxes do not have mandatory marks so its initialization is different from other fields.
    this.editField = this.$element.find('.edit-wrapper.checkbox');
    this.previewField = this.$element.find('.preview-wrapper.checkbox');
    this.printField = this.$element.find('.print-wrapper.checkbox');

    this.setRendered(this.$element, this.fieldViewModel.rendered);
    this.setValidationClass(this.$element, this.getValidationStatusClass());
    this.setWrapperClass(this.$element, this.fieldViewModel.preview);
    this.setViewMode(this.editField, this.previewField, this.printField, this.getFieldViewMode());
    this.disableField();

    this.fieldViewModelSubscription = this.fieldViewModel.subscribe('propertyChanged', (propertyChanged)=> {
      let changedProperty = Object.keys(propertyChanged)[0];
      if (changedProperty === 'preview') {
        this.setViewMode(this.editField, this.previewField, this.printField, this.getFieldViewMode());
        this.disableField(this.editField, this.previewField, this.printField);
        this.setWrapperClass(this.$element, this.fieldViewModel.preview);
      } else if (changedProperty === 'isMandatory') {
        return;
      } else {
        this.executeCommonPropertyChangedHandler(propertyChanged);
      }
    });

    this.validationModelSubscription = this.validationModel[this.fieldViewModel.identifier].subscribe('propertyChanged', (propertyChanged)=> {
      this.executeCommonPropertyChangedHandler(propertyChanged);
    });
  }

  getPrintValue() {
    return this.validationModel[this.fieldViewModel.identifier].value ? this.CHECKED : this.UNCHECKED;
  }

  notifyWhenReady() {
    return true;
  }
}
