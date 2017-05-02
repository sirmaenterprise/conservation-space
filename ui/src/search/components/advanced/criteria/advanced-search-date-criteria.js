import {Component, View, Inject, NgScope, NgElement} from 'app/app';
import {Configurable} from 'components/configurable';
import {Configuration} from 'common/application-config';
import {DatetimePicker} from 'components/datetimepicker/datetimepicker';
import {DynamicDateRange} from 'search/components/advanced/dynamic-date-range/dynamic-date-range';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';
import _ from 'lodash';

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

    this.createPickerConfigurations();
    this.registerOperatorWatcher();
    this.registerDisabledWatcher();
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

  registerDisabledWatcher() {
    this.$scope.$watch(() => {
      return this.config.disabled;
    }, (newState) => {
      this.singlePickerConfig.disabled = newState;
      this.singleDayRangePicker.disabled = newState;
      this.linkedPickersConfig.from.disabled = newState;
      this.linkedPickersConfig.to.disabled = newState;
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

  createPickerConfigurations() {
    this.singlePickerConfig = {
      dateFormat: this.configuration.get(Configuration.UI_DATE_FORMAT),
      timeFormat: this.configuration.get(Configuration.UI_TIME_FORMAT),
      disabled: this.config.disabled,
      placeholder: 'search.date.placeholder',
      defaultValue: this.isDateRange() || this.isWithin() ? '' : this.criteria.value
    };

    this.singleDayRangePicker = {
      dateFormat: this.configuration.get(Configuration.UI_DATE_FORMAT),
      disabled: this.config.disabled,
      hideTime: true,
      listeners: {
        'dp.change': [event => this.onSingleDayRangeDateChange(event)]
      },
      placeholder: 'search.date.placeholder',
      defaultValue: this.isSingleDayRange() ? this.criteria.value[0] : ''
    };

    var fromDefaultValue = this.isBetween() ? this.criteria.value[0] : '';
    var toDefaultValue = this.isBetween() ? this.criteria.value[1] : '';
    this.linkedPickersConfig = {
      from: this.createLinkedPickerConfiguration('from', 'search.date.from.placeholder', fromDefaultValue),
      to: this.createLinkedPickerConfiguration('to', 'search.date.to.placeholder', toDefaultValue)
    };
  }

  onSingleDayRangeDateChange(event) {
    var date = event.date;
    var startDate = date.clone().startOf('day');
    var endDate = date.clone().endOf('day');
    this.criteria.value = [startDate.toISOString(), endDate.toISOString()];
    this.$scope.$apply();
  }

  createLinkedPickerConfiguration(type, placeholder, defaultValue) {
    var onChange = (event) => {
      this.onLinkedPickerDateChange(type, event.date);
    };
    return {
      dateFormat: this.configuration.get(Configuration.UI_DATE_FORMAT),
      timeFormat: this.configuration.get(Configuration.UI_TIME_FORMAT),
      disabled: this.config.disabled,
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