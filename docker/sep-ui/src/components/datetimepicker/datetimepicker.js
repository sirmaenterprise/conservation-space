import {View, Component, Inject, NgScope, NgTimeout, NgElement} from 'app/app';
import 'moment';
import _ from 'lodash';
import 'Eonasdan/bootstrap-datetimepicker';
import {ReusableComponent} from 'components/reusable-component';
import 'Eonasdan/bootstrap-datetimepicker/build/css/bootstrap-datetimepicker.css!';
import 'font-awesome/css/font-awesome.css!';
import './datetimepicker.css!';
import template from './datetimepicker.html!text';

const DP_CLEAR_ACTION = 'clear';
const DP_TODAY_ACTION = 'today';

@Component({
  selector: 'seip-datetime-picker',
  properties: {
    'config': 'config',
    'form': 'form'
  },
  events: ['onChanged']
})
@View({template})
@Inject(NgScope, NgTimeout, NgElement)
export class DatetimePicker extends ReusableComponent {
  constructor($scope, $timeout, $element) {
    const defaultConfig = {
      // these are just some default values, actual values should be taken from Configuration service or from the some other configuration
      // and passed to this component
      dateFormat: 'MMMM/DD/YYYY',
      timeFormat: 'HH:mm',
      hideDate: false,
      hideTime: false,
      dateTimeSeparator: ' ',
      showTodayButton: true,
      showClear: true,
      beforeModelUpdate: _.noop
    };
    super(defaultConfig);
    this.$scope = $scope;
    this.$timeout = $timeout;
    this.ngModel = $element.controller('ngModel');
    this.datepickerElement = $element.find('.datetime');
    this.datepickerElement.datetimepicker(this.actualConfig);
    this.bindToModel();
    this.registerEventListeners();
    this.$element = $element;
    // https://github.com/Eonasdan/bootstrap-datetimepicker/issues/790
    // https://groups.google.com/forum/#!topic/twitter-bootstrap-stackoverflow/cB_rLHt8Hrk
    let pickerParent = this.config.widgetParent;
    this.$element.on('dp.show', (event) => {
      DatetimePicker.updatePickerPosition(pickerParent, $(event.currentTarget));
      this.attachClickHandlers();
    });
  }

  ngOnInit() {
    this.inputElement = this.$element.find('input');
  }

  static updatePickerPosition(pickerParent, element) {
    if (pickerParent) {
      let datePicker = $(pickerParent).find('.bootstrap-datetimepicker-widget:last');
      if (datePicker.hasClass('bottom')) {
        DatetimePicker.calculatePickerPosition(datePicker, element, element.offset().top + element.outerHeight());
      } else if (datePicker.hasClass('top')) {
        DatetimePicker.calculatePickerPosition(datePicker, element, element.offset().top - datePicker.outerHeight());
      }
    }
  }

  static calculatePickerPosition(datePicker, element, top) {
    let left = element.offset().left;
    let datePickerOuterWidth = datePicker.outerWidth();

    if (datePickerOuterWidth + left > DatetimePicker.getWindowWidth()) {
      left = left - datePickerOuterWidth + element.width();
    }

    datePicker.css({
      'top': top + 'px',
      'bottom': 'auto',
      'left': left + 'px'
    });
  }

  static getWindowWidth() {
    return $(window).width();
  }

  createActualConfig() {
    let _this = this;
    let format = this.buildDateTimeFormat(this.config);
    this.config.formatPlaceholder = this.buildFormatPlaceholder(format, this.config.placeholder);
    this.actualConfig = _.pick(this.config, 'showTodayButton', 'showClear', 'useCurrent', 'widgetParent');
    this.actualConfig['format'] = format;
    this.actualConfig['keyBinds'] = {
      enter() {
        let inputValue = _this.inputElement.val();
        let newDate = inputValue === '' ? null : inputValue;
        this.date(newDate).hide();
      },
      // By default datetimepicker overrides delete key behaviour and consumes all delete key events
      // We reset it to default key behaviour in order to track delete key events
      // http://eonasdan.github.io/bootstrap-datetimepicker/Options/#keybinds
      'delete': null
    };
    if (this.config.defaultValue) {
      this.actualConfig['defaultDate'] = new Date(this.config.defaultValue);
    }
  }

  buildDateTimeFormat(config) {
    let format = [];
    if (!config['hideDate']) {
      format.push(config['dateFormat']);
    }
    if (!config['hideTime']) {
      format.push(config['timeFormat']);
    }
    return format.join(config['dateTimeSeparator']);
  }

  buildFormatPlaceholder(format, placeholder) {
    let fmt = format.toLowerCase();
    return placeholder ? `\u0020(${fmt})` : fmt;
  }

  /**
   * Adds two way binding between model and datetimepicker via model watcher and datetimepicker on change event.
   */
  bindToModel() {
    this.datepickerElement.on('dp.change', (e) => {
      let newDate = '';
      if (e.date) {
        this.config.beforeModelUpdate(e.date);
        newDate = e.date.toISOString();
      }
      this.$scope.$apply(() => this.setModelValue(newDate));
    });

    if (this.ngModel) {
      //adding the ngmodel controller to the form controller
      if (this.form) {
        this.form.$addControl(this.ngModel);
      }

      this.$scope.$watch(() => {
        return this.ngModel.$viewValue;
      }, (current, old) => {
        if (current !== old) {
          this.$timeout(() => {
            if (!current) {
              this.datepickerElement.data('DateTimePicker').date(null);
            } else {
              this.datepickerElement.data('DateTimePicker').date(new Date(current));
            }
          });
        }
      }, true);
    }
  }

  registerEventListeners() {
    let listeners = this.config.listeners;
    if (!listeners) {
      return;
    }

    let element = this.datepickerElement;
    Object.keys(listeners).forEach((key) => {
      let handlers = listeners[key];
      if (handlers) {
        handlers.forEach((handler) => element.on(key, handler));
      }
    });
  }

  attachClickHandlers() {
    $('.bootstrap-datetimepicker-widget').on('click', '[data-action]', (e) => {
      let actionName = $(e.currentTarget).attr('data-action');
      if (actionName === DP_CLEAR_ACTION || actionName === DP_TODAY_ACTION) {
        this.datepickerElement.data('DateTimePicker').hide();
      }
    });
  }

  /**
   * Safely sets model value.
   * @param newValue to be set
   */
  setModelValue(newValue) {
    if (this.ngModel) {
      this.ngModel.$setViewValue(newValue);
    }
    this.onChanged({newValue});
  }

  isDisabled() {
    if (this.config.isDisabled) {
      return !!this.config.isDisabled();
    }
    return this.config.disabled;
  }

  ngOnDestroy() {
    this.datepickerElement.off('click', '[data-action]');
    this.datepickerElement.remove();
    this.$element.off();
  }
}
