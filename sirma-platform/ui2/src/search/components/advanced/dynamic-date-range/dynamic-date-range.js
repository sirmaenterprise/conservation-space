import {View, Component, Inject, NgScope} from 'app/app';
import {Configurable} from 'components/configurable';
import {Select} from 'components/select/select';
import {TranslateService} from 'services/i18n/translate-service';
import _ from 'lodash';
import 'moment';
import template from './dynamic-date-range.html!text';
import './dynamic-date-range.css!css';

export const DEFAULT_DATE_STEP = 'today';
export const DEFAULT_DATE_OFFSET = 1;
export const DEFAULT_DATE_OFFSET_TYPE = 'days';

export const TODAY_STEP = 'today';
export const NEXT_STEP = 'next';
export const LAST_STEP = 'last';
export const AFTER_STEP = 'after';
export const BEFORE_STEP = 'before';

export const HOURS_OFFSET = 'hours';
export const DAYS_OFFSET = 'days';
export const WEEKS_OFFSET = 'weeks';
export const MONTHS_OFFSET = 'months';
export const YEARS_OFFSET = 'years';

/**
 * Component for selecting dynamic date ranges and building date ranges from provided date offset configurations.
 *
 * Allows construction of five different types of date steps:
 * 1) Today - from current the day's midnight to the next day's
 * 2) Next - from the current moment to the given offset
 * 3) Last - from the given offset to the current moment
 * 4) After - from the given offset to the future
 * 5) Before - from the past to the given offset
 *
 * Allows specifying five different offset types: hours, days, weeks, months and years
 *
 * Default dynamic date range value is Today.
 *
 * Example configuration:
 *  {
 *    disabled: true // tells if the DOM elements should be rendered as disabled or not
 *  }
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'dynamic-date-range',
  properties: {
    'config': 'config',
    'dateStep': 'date-step',
    'dateOffset': 'date-offset',
    'dateOffsetType': 'date-offset-type'
  }
})
@View({
  template: template
})
@Inject(NgScope, TranslateService)
export class DynamicDateRange extends Configurable {

  constructor($scope, translateService) {
    super({
      disabled: false
    });

    this.$scope = $scope;
    this.translateService = translateService;

    this.assignDefaultModelValues();
    this.configureDateSelects();
    this.assignDisabledWatcher();
    this.assignDateOffsetWatcher();
  }

  assignDefaultModelValues() {
    this.dateStep = this.dateStep || DEFAULT_DATE_STEP;
    this.dateOffset = this.dateOffset || DEFAULT_DATE_OFFSET;
    this.dateOffsetType = this.dateOffsetType || DEFAULT_DATE_OFFSET_TYPE;
  }

  configureDateSelects() {
    this.dateStepSelectConfig = {
      disabled: this.config.disabled,
      selectOnClose: true,
      data: [{
        id: TODAY_STEP,
        text: this.translateService.translateInstant('search.daterange.dynamic.step.today')
      }, {
        id: NEXT_STEP,
        text: this.translateService.translateInstant('search.daterange.dynamic.step.next')
      }, {
        id: LAST_STEP,
        text: this.translateService.translateInstant('search.daterange.dynamic.step.last')
      }, {
        id: AFTER_STEP,
        text: this.translateService.translateInstant('search.daterange.dynamic.step.after')
      }, {
        id: BEFORE_STEP,
        text: this.translateService.translateInstant('search.daterange.dynamic.step.before')
      }]
    };

    this.dateOffsetTypeSelectConfig = {
      disabled: this.config.disabled,
      selectOnClose: true,
      data: [{
        id: HOURS_OFFSET,
        text: this.translateService.translateInstant('search.daterange.dynamic.type.hours')
      }, {
        id: DAYS_OFFSET,
        text: this.translateService.translateInstant('search.daterange.dynamic.type.days')
      }, {
        id: WEEKS_OFFSET,
        text: this.translateService.translateInstant('search.daterange.dynamic.type.weeks')
      }, {
        id: MONTHS_OFFSET,
        text: this.translateService.translateInstant('search.daterange.dynamic.type.months')
      }, {
        id: YEARS_OFFSET,
        text: this.translateService.translateInstant('search.daterange.dynamic.type.years')
      }]
    };
  }

  assignDisabledWatcher() {
    this.$scope.$watch(() => {
      return this.config.disabled;
    }, (newState) => {
      this.dateStepSelectConfig.disabled = newState;
      this.dateOffsetTypeSelectConfig.disabled = newState;
    });
  }

  /**
   * Assigns a watcher for the date offset property. It checks if the new value is valid and reverts it if not. Invalid
   * offset is anything different from empty value or a string with characters containing more than digits.
   */
  assignDateOffsetWatcher() {
    var regex = new RegExp('^[0-9]*$');
    this.$scope.$watch(() => {
      return this.dateOffset;
    }, (newValue, oldValue) => {
      // If newValue is null - it means the input is empty and there is nothing to validate.
      if (newValue === null) {
        return;
      }
      // If the new value is not valid according the regex - try to revert it
      if (!regex.test(newValue)) {
        if (regex.test(oldValue)) {
          // Revert the offset with the old value
          this.dateOffset = oldValue;
        } else {
          // If the old is invalid too - use the default
          this.dateOffset = DEFAULT_DATE_OFFSET;
        }
      }
    });
  }

  isToday() {
    return this.dateStep === TODAY_STEP;
  }

  static buildDateRange(dateOffset) {
    var start;
    var end;

    switch (dateOffset.dateStep) {
      case 'today':
        start = moment().startOf('day');
        end = start.clone().add(1, 'day');
        break;
      case 'next':
        start = moment();
        end = start.clone().add(dateOffset.offset, dateOffset.offsetType);
        break;
      case 'last':
        end = moment();
        start = end.clone().subtract(dateOffset.offset, dateOffset.offsetType);
        break;
      case 'after':
        start = moment().add(dateOffset.offset, dateOffset.offsetType);
        break;
      case 'before':
        end = moment().subtract(dateOffset.offset, dateOffset.offsetType);
        break;
      default:
        break;
    }
    return [start && start.toISOString() || '', end && end.toISOString() || ''];
  }

}
