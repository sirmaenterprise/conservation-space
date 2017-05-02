import {View, Component, Inject, NgElement} from 'app/app';
import {FormControl} from 'form-builder/form-control';
import _ from 'lodash';
import template from 'form-builder/radiobutton-group/radiobutton-group.html!text';

@Component({
  selector: 'seip-radiobutton-group',
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
export class RadiobuttonGroup extends FormControl {
  constructor($element) {
    super();
    this.$element = $element;
    //initialize mutation observer, so in order to property initialize the radiobutton group
    var MutationObserver = window.MutationObserver || window.WebKitMutationObserver;
    var observer = new MutationObserver(this.mutationHandler);
    observer.totalOptions = this.fieldViewModel.control.controlFields.length;
    observer.radiobuttonControl = this;
    var obsConfig = {childList: true, characterData: false, attributes: false, subtree: true};

    this.$element.each(function () {
      observer.observe(this, obsConfig);
    });
  }

  mutationHandler(mutationRecords) {
    let loadedOptions = 0;

    mutationRecords.forEach((mutation) => {
      if (typeof mutation.addedNodes === "object" && mutation.removedNodes.length === 0) {
        loadedOptions++;
        if (loadedOptions === this.totalOptions) {
          this.radiobuttonControl.init();
        }
      }
    });
  }

  init() {
    this.mandatoryMark = this.$element.find('.mandatory-mark');
    this.inputField = this.$element.find('.form-field.radiobutton-field');
    this.labelField = this.$element.find('.edit-label.radio');
    this.previewField = this.$element.find('.preview-field.radiobutton-group');
    this.printField = this.$element.find('.print-field.radiobutton-group');
    this.addClass(this.$element, this.fieldViewModel.control.controlParams.layout);
    this.setRendered(this.$element, this.fieldViewModel.rendered);
    this.renderMark(this.mandatoryMark, this.renderMandatoryMark());
    this.setValidationClass(this.$element, this.getValidationStatusClass());
    this.setWrapperClass(this.$element, this.fieldViewModel.preview);
    this.setViewMode();
    this.setDisabled(this.fieldViewModel.disabled || this.fieldViewModel.preview);

    this.fieldViewModelSubscription = this.fieldViewModel.subscribe('propertyChanged', (propertyChanged) => {
      let changedProperty = Object.keys(propertyChanged)[0];
      if (changedProperty === 'preview') {
        this.setViewMode();
        this.setDisabled(propertyChanged.preview);
        this.renderMark(this.mandatoryMark, this.renderMandatoryMark());
      } else if (changedProperty === 'disabled') {
        this.setDisabled(propertyChanged.disabled);
        this.setLabelClass();
      } else {
        this.executeCommonPropertyChangedHandler(propertyChanged);
      }
    });
    this.validationModelSubscription = this.validationModel[this.fieldViewModel.identifier].subscribe('propertyChanged', (propertyChanged) => {
      this.executeCommonPropertyChangedHandler(propertyChanged);
    });
  }

  setLabelClass() {
    if (this.fieldViewModel.disabled || this.fieldViewModel.preview) {
      this.labelField.each(function () {
        $(this).addClass('state-disabled');
      });
    } else {
      this.labelField.each(function () {
        $(this).removeClass('state-disabled');
      });
    }
  }

  setViewMode() {
    let fieldViewMode = this.getFieldViewMode();
    if (fieldViewMode === 'EDIT') {
      this.labelField.each(function () {
        $(this).removeClass('hidden');
      });
      this.previewField.addClass('hidden');
      this.printField.addClass('hidden');
    } else if (fieldViewMode === 'PREVIEW') {
      this.labelField.each(function () {
        $(this).addClass('hidden');
      });
      this.previewField.removeClass('hidden');
      this.printField.addClass('hidden');
    } else if (fieldViewMode === 'PRINT') {
      this.labelField.each(function () {
        $(this).addClass('hidden');
      });
      this.previewField.addClass('hidden');
      this.printField.removeClass('hidden');
    }
  }

  setDisabled(disabled) {
    this.inputField.each(function () {
      $(this).attr({disabled: disabled});
    });
    this.setLabelClass();
  }

  getLabel() {
    let value = this.validationModel[this.fieldViewModel.identifier].value;
    let controlField = _.find(this.fieldViewModel.control.controlFields, (field) => {
      return field.identifier === value;
    });
    return controlField ? controlField.label : '';
  }
}
