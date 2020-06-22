import {Inject, View, Component, NgElement} from 'app/app';
import {FormControl} from 'form-builder/form-control';
import {FormWrapper, LAYOUT} from 'form-builder/form-wrapper';
import {ModelUtils} from 'models/model-utils';
import 'components/instance-selector/instance-selector';
import {MULTIPLE_SELECTION, SINGLE_SELECTION} from 'search/search-selection-modes';
import {DEFAULT_VALUE_PATTERN} from 'form-builder/validation/calculation/calculation';
import {MODE_EDIT, MODE_PREVIEW} from 'idoc/idoc-constants';
import {EventEmitter} from 'common/event-emitter';

import template from './object-control.html!text';

@Component({
  selector: 'seip-object-control',
  properties: {
    'formWrapper': 'form-wrapper',
    'identifier': 'identifier'
  }
})
@View({
  template
})
@Inject(NgElement)
export class ObjectControl extends FormControl {
  constructor($element) {
    super();
    this.$element = $element;
    this.eventEmitter = new EventEmitter();
    let loadedSubscription = this.eventEmitter.subscribe('instanceSelectorRendered', () => {
      loadedSubscription.unsubscribe();
      this.formEventEmitter.publish('formControlLoaded', {identifier: this.fieldViewModel.identifier});
    });
  }

  ngOnInit() {
    if (!this.validationModel[this.fieldViewModel.identifier].value) {
      this.validationModel[this.fieldViewModel.identifier].value = ModelUtils.getEmptyObjectPropertyValue();
    }

    // additional margin applied when there are no values, because the element having values increases by 5px in height.
    if (FormWrapper.isPrintMode(this.formWrapper.formViewMode) && this.widgetConfig.layout === LAYOUT.VERTICAL && this.validationModel[this.fieldViewModel.identifier].value.length === 0) {
      this.$element.css({'margin-bottom': '5px'});
    }

    let controlParams = ModelUtils.getControl(this.fieldViewModel.control, this.fieldViewModel.controlId).controlParams;
    let predefinedTypes = ObjectControl.getPredefinedTypes(controlParams);
    let pickerRestrictions = ObjectControl.getPickerRestrictions(controlParams);
    this.instanceSelectorConfig = this.getInstanceSelectorConfig(predefinedTypes, pickerRestrictions);

    this.mandatoryMark = this.$element.find('.mandatory-mark');

    this.setRendered(this.$element, this.fieldViewModel.rendered);
    this.addClass(this.$element, this.getValidationStatusClass());
    this.renderMark(this.mandatoryMark, this.renderMandatoryMark());

    this.fieldViewModelSubscription = this.fieldViewModel.subscribe('propertyChanged', (propertyChanged) => {
      if (Object.keys(propertyChanged)[0] === 'preview') {
        this.renderMark(this.mandatoryMark, this.renderMandatoryMark());
        this.instanceSelectorConfig.mode = propertyChanged.preview ? MODE_PREVIEW : MODE_EDIT;
      } else {
        this.executeCommonPropertyChangedHandler(propertyChanged);
      }
    });

    this.validationModelSubscription = this.validationModel[this.fieldViewModel.identifier].subscribe('propertyChanged', (propertyChanged) => {
      this.executeCommonPropertyChangedHandler(propertyChanged);
    });
  }

  static getPredefinedTypes(controlParams) {
    if (controlParams && controlParams.range) {
      return controlParams.range.split(',').map((rangeClass) => {
        return rangeClass.trim();
      });
    }
  }

  static getPickerRestrictions(controlParams) {
    if (controlParams && controlParams.restrictions) {
      return JSON.parse(controlParams.restrictions);
    }
  }

  static getFieldViewMode(formViewMode, fieldViewModel) {
    let mode;
    if(FormWrapper.isPreviewMode(formViewMode) || FormWrapper.isPrintMode(formViewMode)) {
      mode = MODE_PREVIEW;
    } else {
      mode = fieldViewModel.preview ? MODE_PREVIEW : MODE_EDIT;
    }
    return mode;
  }

  getInstanceSelectorConfig(predefinedTypes, pickerRestrictions) {
    let setEditedByUserCallback;
    if (this.isControl(DEFAULT_VALUE_PATTERN)) {
      setEditedByUserCallback = () => {
        this.fieldViewModel.editedByUser = true;
      };
    }

    let excludedObjects = [];
    if (this.objectId) {
      excludedObjects.push(this.objectId);
    }
    return {
      objectId: this.objectId,
      owningRelatedObjectId: this.fieldViewModel.owningRelatedObjectId,
      subPropertyName: this.fieldViewModel.subPropertyName,
      propertyName: this.fieldViewModel.identifier,
      selection: this.fieldViewModel.multivalue ? MULTIPLE_SELECTION : SINGLE_SELECTION,
      mode: ObjectControl.getFieldViewMode(this.formWrapper.formViewMode, this.fieldViewModel),
      instanceHeaderType: this.widgetConfig.instanceLinkType,
      excludedObjects,
      onSelectionChangedCallback: setEditedByUserCallback,
      predefinedTypes,
      pickerRestrictions,
      label: this.fieldViewModel.label,
      formViewMode: this.formWrapper.formViewMode,
      eventEmitter: this.eventEmitter,
      isNewInstance: this.isNewInstance,
      fieldIdentifier: this.fieldViewModel.identifier,
      fieldUri: this.fieldViewModel.uri,
      definitionId: this.definitionId
    };
  }
}