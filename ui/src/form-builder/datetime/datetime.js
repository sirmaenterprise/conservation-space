import {View, Component, Inject, NgScope, NgElement} from 'app/app';
import {FormControl} from 'form-builder/form-control';
import {Configuration} from 'common/application-config';
import {DatetimePicker} from 'components/datetimepicker/datetimepicker';
import {MomentAdapter} from 'adapters/moment-adapter';
import template from './datetime.html!text';

@Component({
  selector: 'seip-datetime',
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
@Inject(Configuration, NgScope, MomentAdapter, NgElement)
export class Datetime extends FormControl {

  constructor(configuration, $scope, momentAdapter, $element) {
    super();
    this.$scope = $scope;
    this.$element = $element;
    this.configuration = configuration;
    this.momentAdapter = momentAdapter;
    this.isDatetimeField = this.fieldViewModel.dataType === 'datetime';
    this.config = this.getFieldConfig();
    this.pattern = this.configuration.get(Configuration.UI_DATE_FORMAT);
    if (this.isDatetimeField) {
      this.pattern += ' ' + this.configuration.get(Configuration.UI_TIME_FORMAT);
    }
  }

  getFieldConfig() {
    return {
      defaultValue: this.validationModel[this.fieldViewModel.identifier].value,
      dateFormat: this.configuration.get(Configuration.UI_DATE_FORMAT),
      timeFormat: this.configuration.get(Configuration.UI_TIME_FORMAT),
      hideTime: !this.isDatetimeField,
      cssClass: 'form-field datetime-field',
      disabled: this.fieldViewModel.disabled,
      widgetParent: 'body'
    };
  }

  setDisabled() {
    this.config.disabled = this.fieldViewModel.disabled;
  }

  getFormattedDate() {
    return this.momentAdapter.format(this.validationModel[this.fieldViewModel.identifier].value, this.pattern);
  }

  ngOnInit() {
    this.initElement();

    this.fieldViewModelSubscription = this.fieldViewModel.subscribe('propertyChanged', (propertyChanged) => {
      if (Object.keys(propertyChanged)[0] === 'disabled') {
        this.setDisabled();
      }
      this.executeCommonPropertyChangedHandler(propertyChanged);
    });

    this.validationModelSubscription = this.validationModel[this.fieldViewModel.identifier].subscribe('propertyChanged', (propertyChanged) => {
      this.executeCommonPropertyChangedHandler(propertyChanged);
    });
  }
}
