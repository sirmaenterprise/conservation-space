import {View, Component, Inject, NgElement} from 'app/app';
import {FormControl} from 'form-builder/form-control';
import {InstanceSelector} from 'components/instance-selector/instance-selector';
import {MULTIPLE_SELECTION} from 'search/search-selection-modes';
import {MODE_PREVIEW} from 'idoc/idoc-constants';
import template from './resource.html!text';

@Component({
  selector: 'seip-resource',
  properties: {
    'fieldViewModel': 'field-view-model',
    'validationModel': 'validation-model',
    'validationService': 'validation-service',
    'flatFormViewModel': 'flat-form-view-model',
    'objectId': 'object-id',
    'widgetConfig': 'widget-config'
  }
})
@View({
  template: template
})
@Inject(NgElement)
export class Resource extends FormControl {
  constructor($element) {
    super();
    this.$element = $element;
    this.instanceSelectorConfig = {
      selection: MULTIPLE_SELECTION,
      mode: MODE_PREVIEW
    };
  }

  ngOnInit() {
    this.mandatoryMark = this.$element.find('.mandatory-mark');
    this.setRendered(this.$element, this.fieldViewModel.rendered);
    this.setWrapperClass(this.$element, this.fieldViewModel.preview);
    this.renderMark(this.mandatoryMark, this.renderMandatoryMark());

    this.fieldViewModelSubscription = this.fieldViewModel.subscribe('propertyChanged', (propertyChanged) => {
      let changedProperty = Object.keys(propertyChanged)[0];
      if (changedProperty === 'preview') {
        this.renderMark(this.mandatoryMark, this.renderMandatoryMark());
        return;
      }
      this.executeCommonPropertyChangedHandler(propertyChanged);
    });

    this.validationModelSubscription = this.validationModel[this.fieldViewModel.identifier].subscribe('propertyChanged', (propertyChanged) => {
      this.executeCommonPropertyChangedHandler(propertyChanged);
    });
  }
}
