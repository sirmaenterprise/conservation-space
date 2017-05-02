import {View, Component, Inject, NgElement} from 'app/app';
import {FormControl} from 'form-builder/form-control';
import {InstanceSelector} from 'components/instance-selector/instance-selector';
import {MULTIPLE_SELECTION} from 'search/search-selection-modes';
import {MODE_PREVIEW} from 'idoc/idoc-constants';
import {InstanceModel, InstanceModelProperty} from 'models/instance-model';
import template from './user.html!text';

@Component({
  selector: 'seip-user',
  properties: {
    'fieldViewModel': 'field-view-model',
    'validationModel': 'validation-model',
    'validationService': 'validation-service',
    'objectId': 'object-id',
    'widgetConfig': 'widget-config'
  }
})
@View({
  template: template
})
@Inject(NgElement)
export class User extends FormControl {
  constructor($element) {
    super();
    this.$element = $element;
    this.instanceSelectorConfig = {
      selection: MULTIPLE_SELECTION,
      mode: MODE_PREVIEW
    };
  }

  ngOnInit() {
    this.instanceSelector = this.$element.find('.form-field');
    this.mandatoryMark = this.$element.find('.mandatory-mark');

    this.setRendered(this.$element, this.fieldViewModel.rendered);
    this.renderMark(this.mandatoryMark, this.renderMandatoryMark());

    this.fieldViewModelSubscription = this.fieldViewModel.subscribe('propertyChanged', (propertyChanged) => {
      this.COMMON_PROPERTY_CHANGED_HANDLERS[Object.keys(propertyChanged)[0]]();
    });
  }
}
