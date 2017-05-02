import {Inject, View, Component, NgElement} from 'app/app';
import {FormControl} from 'form-builder/form-control';
import {InstanceSelector} from 'components/instance-selector/instance-selector';
import {MULTIPLE_SELECTION, SINGLE_SELECTION} from 'search/search-selection-modes';
import {MODE_EDIT, MODE_PREVIEW} from 'idoc/idoc-constants';
import template from './object-control.html!text';

@Component({
  selector: 'seip-object-control',
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
class ObjectControl extends FormControl {
  constructor($element) {
    super();
    this.$element = $element;
    this.validationModel[this.fieldViewModel.identifier].value = this.validationModel[this.fieldViewModel.identifier].value || [];
    let predefinedTypes;

    if (this.fieldViewModel.control.controlParams && this.fieldViewModel.control.controlParams.range) {
      predefinedTypes = this.fieldViewModel.control.controlParams.range.split(',').map((rangeClass) => {
        return rangeClass.trim();
      });
    }
    this.instanceSelectorConfig = {
      selection: this.fieldViewModel.multivalue ? MULTIPLE_SELECTION : SINGLE_SELECTION,
      mode: this.fieldViewModel.preview ? MODE_PREVIEW : MODE_EDIT,
      instanceHeaderType: this.widgetConfig.instanceLinkType,
      predefinedTypes
    };

  }

  ngOnInit() {
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
}
