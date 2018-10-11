import {Component, View, Inject, NgScope, NgElement} from 'app/app';
import {Configurable} from 'components/configurable';
import {Configuration} from 'common/application-config';
import 'components/datetimepicker/datetimepicker';
import 'search/components/advanced/dynamic-date-range/dynamic-date-range';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';

import './advanced-search-date-criteria.css!css';
import template from './advanced-search-date-criteria.html!text';

/**
 * Criteria component for rendering different date pickers based on the criteria operator. This includes:
 *  - Single date/time picker
 *  - Single date picker for a single day range
 *  - Two date/time pickers for choosing a date range
 *
 * Providing a criteria is essential for this component to work! The component is also configurable -> example
 * configuration:
 *  config: {
 *    disabled: false
 *  }
 *
 *  @author Mihail Radkov
 */
@Component({
  selector: 'seip-advanced-search-date-criteria',
  properties: {
    'config': 'config',
    'criteria': 'criteria'
  }
})
@View({
  template: template
})
@Inject(NgScope, NgElement, Configuration)
export class AdvancedSearchDateCriteria extends Configurable {

  constructor($scope, $element, configuration) {
    super({
      disabled: false
    });
    this.$scope = $scope;
    this.$element = $element;
    this.configuration = configuration;
  }

  ngOnInit() {
    this.assignValidModel();
    this.createPickerConfigurations();
    this.registerOperatorWatcher();
  }

  registerOperatorWatcher() {
    this.$scope.$watch(() => {
      return this.criteria.operator;
    }, (newOperator, oldOperator) => {
      if (newOperator !== oldOperator) {
        this.resetModelValue();
      }
    });
  }

  /**
   * Creates correct criteria value model when the operator is changed. If the component is selecting single date then
   * it should be a string and not an array. If the component should choose a date range - the model should be an array.
   */
  resetModelValue() {
    if (this.isDateRange()) {
      this.criteria.value = ['', ''];
    } else if (this.isWithin()) {
      this.criteria.value = ['', '', ''];
    } else {
      this.criteria.value = '';
      this.singlePickerConfig.defaultValue = '';
    }
  }

  assignValidModel() {
    if (this.isDateRange() && (!(this.criteria.value instanceof Array) || this.criteria.value.length !== 2)) {
      this.criteria.value = ['', ''];
    } else if (this.isWithin() && (!(this.criteria.value instanceof Array) || this.criteria.value.length !== 3)) {
      this.criteria.value = ['', '', ''];
    }
  }

  createPickerConfigurations() {
    this.singlePickerConfig = {
      dateFormat: this.configuration.get(Configuration.UI_DATE_FORMAT),
      timeFormat: this.configuration.get(Configuration.UI_TIME_FORMAT),
      isDisabled: () => this.config.disabled,
      defaultValue: this.isDateRange() || this.isWithin() ? '' : this.criteria.value
    };

    this.singleDayRangePicker = {
      dateFormat: this.configuration.get(Configuration.UI_DATE_FORMAT),
      isDisabled: () => this.config.disabled,
      hideTime: true,
      listeners: {
        'dp.change': [event => this.onSingleDayRangeDateChange(event)]
      },
      defaultValue: this.isSingleDayRange() ? this.criteria.value[0] : ''
    };

    let fromDefaultValue = this.isBetween() ? this.criteria.value[0] : '';
    let toDefaultValue = this.isBetween() ? this.criteria.value[1] : '';
    this.linkedPickersConfig = {
      from: this.createLinkedPickerConfiguration('from', 'search.date.from.placeholder', fromDefaultValue),
      to: this.createLinkedPickerConfiguration('to', 'search.date.to.placeholder', toDefaultValue)
    };
  }

  onSingleDayRangeDateChange(event) {
    let date = event.date;
    let startDate = date.clone().startOf('day');
    let endDate = date.clone().endOf('day');
    this.criteria.value = [startDate.toISOString(), endDate.toISOString()];
    this.$scope.$apply();
  }

  createLinkedPickerConfiguration(type, placeholder, defaultValue) {
    let onChange = (event) => {
      this.onLinkedPickerDateChange(type, event.date);
    };
    return {
      dateFormat: this.configuration.get(Configuration.UI_DATE_FORMAT),
      timeFormat: this.configuration.get(Configuration.UI_TIME_FORMAT),
      isDisabled: () => this.config.disabled,
      useCurrent: false,
      listeners: {
        'dp.change': [onChange]
      },
      placeholder: placeholder,
      defaultValue: defaultValue
    };
  }

  onLinkedPickerDateChange(type, date) {
    if (type === 'from') {
      this.dateToPicker.data('DateTimePicker').minDate(date);
    } else {
      this.dateFromPicker.data('DateTimePicker').maxDate(date);
    }
  }

  isBetween() {
    return this.criteria.operator === AdvancedSearchCriteriaOperators.IS_BETWEEN.id;
  }

  isSingleDayRange() {
    return this.criteria.operator === AdvancedSearchCriteriaOperators.IS.id;
  }

  isDateRange() {
    return this.isBetween() || this.isSingleDayRange();
  }

  isWithin() {
    return this.criteria.operator === AdvancedSearchCriteriaOperators.IS_WITHIN.id;
  }

  get dateFromPicker() {
    return this.$element.find('.date-from .datetime');
  }

  get dateToPicker() {
    return this.$element.find('.date-to .datetime');
  }
}