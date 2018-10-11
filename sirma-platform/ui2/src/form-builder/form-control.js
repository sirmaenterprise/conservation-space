import 'form-builder/common/validation-message';
import 'components/hint/hint';
import {FormWrapper} from 'form-builder/form-wrapper';
import {ModelUtils} from 'models/model-utils';

/**
 * Common class for all form controls.
 *
 * @author svelikov
 */
export class FormControl {

  constructor() {
    this.fieldViewModel = this.formWrapper.fieldsMap[this.identifier];
    this.validationModel = this.formWrapper.formConfig.models.validationModel;
    this.flatFormViewModel = this.formWrapper.getViewModel();
    this.form = this.formWrapper.objectDataForm;
    this.validationService = this.formWrapper.validationService;
    this.widgetConfig = this.formWrapper.config;
    this.objectId = this.formWrapper.formConfig.models.id;
    this.definitionId = this.formWrapper.formConfig.models.definitionId;
    this.isNewInstance = this.formWrapper.formConfig.models.isNewInstance;
    this.formEventEmitter = this.formWrapper.eventEmitter;
  }

  /**
   * Initialize the field to its initial state.
   */
  initElement() {
    this.editField = this.$element.find('.edit-field');
    this.previewField = this.$element.find('.preview-field');
    this.printField = this.$element.find('.print-field');
    this.mandatoryMark = this.$element.find('.mandatory-mark');

    this.setRendered(this.$element, this.fieldViewModel.rendered);
    this.renderMark(this.mandatoryMark, this.renderMandatoryMark());
    this.setValidationClass(this.$element, this.getValidationStatusClass());
    this.setWrapperClass(this.$element, this.fieldViewModel.preview);
    this.setViewMode(this.editField, this.previewField, this.printField, this.getFieldViewMode());
    this.disableField();
  }

  /**
   * Custom hook implementation similar to ``ngAfterViewInit``, that uses the same logic, but only called when in print/export to pdf mode.
   */
  ngAfterViewInit() {
    if (this.notifyWhenReady()) {
      this.formEventEmitter.publish('formControlLoaded', {identifier: this.fieldViewModel.identifier});
    }
  }

  renderMandatoryMark() {
    return this.fieldViewModel.isMandatory && !this.fieldViewModel.preview;
  }

  getFieldViewMode() {
    let formViewMode = this.widgetConfig.formViewMode || FormWrapper.FORM_VIEW_MODE_EDIT;
    let isPreviewField = this.fieldViewModel.preview && this.fieldViewModel.preview === true ?
      FormWrapper.FORM_VIEW_MODE_PREVIEW : FormWrapper.FORM_VIEW_MODE_EDIT;
    let key = `${formViewMode}_${isPreviewField}`;
    return FormControl.FIELD_MODES[key];
  }

  isEditField() {
    return this.getFieldViewMode() === FormControl.VIEW_MODES.EDIT;
  }

  isPreviewField() {
    return this.getFieldViewMode() === FormControl.VIEW_MODES.PREVIEW;
  }

  isPrintField() {
    return this.getFieldViewMode() === FormControl.VIEW_MODES.PRINT;
  }

  getValidationStatusClass() {
    let valid = this.validationModel[this.fieldViewModel.identifier] && this.validationModel[this.fieldViewModel.identifier].valid;
    return valid !== undefined && !valid ? 'has-error' : '';
  }

  validateForm() {
    this.validationService.validate(this.validationModel, this.flatFormViewModel, this.objectId, false, this, this.definitionId, this.formWrapper.instanceId);
  }

  /**
   * Field placeholders are set only if given condition is met. By default this should be when the labels are hidden.
   * @returns {string}
   */
  getPlaceholder() {
    return this.widgetConfig.labelPosition === this.widgetConfig.showFieldPlaceholderCondition ? this.fieldViewModel.label : '';
  }

  showTooltip() {
    if (this.widgetConfig.formViewMode === FormWrapper.FORM_VIEW_MODE_PREVIEW) {
      return false;
    }
    if (!this.widgetConfig.enableHint) {
      return false;
    }

    return !!this.fieldViewModel.tooltip;
  }

  setRendered(element, rendered) {
    this.toggleClass(element, 'hidden', !rendered);
  }

  renderMark(mandatoryMarkElement, shouldRender) {
    this.toggleClass(mandatoryMarkElement, 'hidden', !shouldRender);
  }

  setViewMode(editField, previewField, printField, mode) {
    if (mode === FormControl.VIEW_MODES.EDIT) {
      this.removeClass(editField, 'hidden');
      this.addClass(previewField, 'hidden');
      this.addClass(printField, 'hidden');
    } else if (mode === FormControl.VIEW_MODES.PREVIEW) {
      this.addClass(editField, 'hidden');
      this.removeClass(previewField, 'hidden');
      this.addClass(printField, 'hidden');
    } else if (mode === FormControl.VIEW_MODES.PRINT) {
      this.addClass(editField, 'hidden');
      this.addClass(previewField, 'hidden');
      this.removeClass(printField, 'hidden');
    }
  }

  setValidationClass(element, valid) {
    if (valid === 'has-error') {
      this.addClass(element, valid);
    } else if (valid === false) {
      this.addClass(element, 'has-error');
    } else {
      this.removeClass(element, 'has-error');
    }
  }

  disableField() {
    let fieldViewMode = this.getFieldViewMode();
    this.editField.attr({disabled: this.fieldViewModel.disabled});

    if (this.fieldViewModel.disabled || this.fieldViewModel.preview) {
      if (fieldViewMode === FormControl.VIEW_MODES.PRINT) {
        this.addClass(this.printField, 'state-disabled');
      } else if (fieldViewMode === FormControl.VIEW_MODES.PREVIEW) {
        this.addClass(this.previewField, 'state-disabled');
        this.previewField.find('.preview-field').attr({disabled: true});
      } else if (fieldViewMode === FormControl.VIEW_MODES.EDIT) {
        this.addClass(this.editField, 'state-disabled');
        this.editField.find('.edit-field').attr({disabled: true});
      }
    }
  }

  setWrapperClass(element, previewMode) {
    this.toggleClass(element, 'preview-field-wrapper', previewMode);
  }

  toggleClass(element, newClass, shouldAdd) {
    if (shouldAdd) {
      this.addClass(element, newClass);
    } else {
      this.removeClass(element, newClass);
    }
  }

  addClass(element, newClass) {
    element.addClass(newClass);
  }

  removeClass(element, removedClass) {
    element.removeClass(removedClass);
  }

  //removes unneccesary duplication in different fields.
  COMMON_PROPERTY_CHANGED_HANDLERS = {
    rendered: () => {
      this.toggleClass(this.$element, 'hidden', !this.fieldViewModel.rendered);
    },
    preview: () => {
      this.setViewMode(this.editField, this.previewField, this.printField, this.getFieldViewMode());
      this.renderMark(this.mandatoryMark, this.renderMandatoryMark());
      this.setWrapperClass(this.$element, this.fieldViewModel.preview);
    },
    disabled: () => {
      this.disableField();
    },
    isMandatory: () => {
      this.toggleClass(this.mandatoryMark, 'hidden', !this.renderMandatoryMark());
    },
    value: () => {
      this.validateForm();
    },
    valid: (propertyChanged) => {
      this.setValidationClass(this.$element, propertyChanged.valid);
    }
  };

  executeCommonPropertyChangedHandler(propertyChanged) {
    let changedProperty = Object.keys(propertyChanged)[0];
    if (this.isSupportedHandler(changedProperty)) {
      this.COMMON_PROPERTY_CHANGED_HANDLERS[changedProperty](propertyChanged);
    }
  }

  isSupportedHandler(handlerName) {
    return typeof this.COMMON_PROPERTY_CHANGED_HANDLERS[handlerName] === 'function';
  }

  isControl(type) {
    if (!this.fieldViewModel.control || !ModelUtils.getControl(this.fieldViewModel.control, type)) {
      return false;
    }
    return true;
  }

  ngOnDestroy() {
    if (this.fieldViewModelSubscription) {
      this.fieldViewModelSubscription.unsubscribe();
      this.fieldViewModelSubscription = null;
    }

    if (this.validationModelSubscription) {
      this.validationModelSubscription.unsubscribe();
      this.validationModelSubscription = null;
    }

    this.removeElement(this.editField);
    this.removeElement(this.previewField);
    this.removeElement(this.printField);
    this.removeElement(this.mandatoryMark);
    this.removeElement(this.element);
    this.removeElement(this.$element);

    this.validationModel = {};
  }

  removeElement(element) {
    if (element) {
      element.empty();
      element.remove();
    }
  }

  /**
   * Checks if the control is a region or object control as either have different ``formControlLoaded`` event emitting logic.
   *
   * @returns {boolean} true if not region or object control.
   */
  notifyWhenReady() {
    return false;
  }
}

FormControl.FIELD_MODES = {
  EDIT_PREVIEW: 'PREVIEW',
  EDIT_EDIT: 'EDIT',
  PREVIEW_PREVIEW: 'PREVIEW',
  PREVIEW_EDIT: 'PREVIEW',
  PRINT_PREVIEW: 'PRINT',
  PRINT_EDIT: 'PRINT'
};
FormControl.VIEW_MODES = {
  EDIT: 'EDIT',
  PREVIEW: 'PREVIEW',
  PRINT: 'PRINT'
};