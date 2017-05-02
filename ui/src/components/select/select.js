import {View, Component, Inject, NgElement, NgScope, NgTimeout} from 'app/app';
import {CustomDataAdapter} from 'components/select/adapters/custom-data-adapter';
import {ReusableComponent} from 'components/reusable-component';
import {NavigatorAdapter} from 'adapters/navigator-adapter';
import {JsonUtil} from 'common/json-util';
import _ from 'lodash';
import 'select2';

import 'select2/css/select2.css!';
import './select.css!';
import selectTemplate from './select.html!text';

@Component({
  selector: 'seip-select',
  properties: {
    'config': 'config',
    'form': 'form'
  }
})
@View({
  template: selectTemplate
})
@Inject(NgElement, NgScope, NgTimeout)
export class Select extends ReusableComponent {
  constructor($element, $scope, $timeout) {
    super({
      width: 'resolve'
    });
    this.$scope = $scope;
    this.$timeout = $timeout;
    this.$element = $element.find('select');
    this.ngModel = $element.controller('ngModel');
    if (this.form) {
      //adding the ngmodel controller to the form controller
      this.form.$addControl(this.ngModel);
    }

    this.initSelect();
  }

  initSelect() {
    this.$element.select2(this.actualConfig);

    // manually destroy the element to prevent memory leak
    this.$element.on('remove', () => {
      if (this.$element.data('select2')) {
        this.$element.select2('destroy');
      }
    });

    // Append listeners
    if (this.config.listeners) {
      this.appendListeners(this.config.listeners);
    }
    this.bindToModel();
    this.preventAutoOpening();
    this.ensureFocusOnTyping(this.$element);
    this.enforceMinimumSelectionLength(this.config.minimumSelectionLength);
  }

  createActualConfig() {
    this.actualConfig = _.pick(this.config, [
      'tags', 'mapper', 'width', 'multiple', 'placeholder', 'allowClear',
      'data', 'defaultValue', 'defaultToSingleValue', 'defaultToFirstValue', 'selectOnClose', 'disabled', 'closeOnSelect'
    ]);

    // Load data asynchronously when select is open
    if (this.config.dataLoader) {
      this.actualConfig.dataAdapter = CustomDataAdapter;

      let ajaxConfig = {};
      ajaxConfig.transport = (params, success, failure) => {
        let loader = this.config.dataLoader;
        if (_.isFunction(loader)) {
          loader = loader(params);
        }
        loader.then(success, failure);
      };

      ajaxConfig.processResults = (data, params) => {
        let results = data;
        if (this.config.dataConverter) {
          results = this.config.dataConverter(data, params);
        }

        if (results && results.results) {
          return results;
        }
        return {results: results};
      };

      JsonUtil.copyProperty(ajaxConfig, 'delay', this.config);
      this.actualConfig.ajax = ajaxConfig;
    }

    if (this.config.escapeMarkup) {
      this.actualConfig.escapeMarkup = this.config.escapeMarkup;
    } else {
      this.actualConfig.escapeMarkup = (markup) => {
        return markup;
      };
    }

    this.actualConfig.templateResult = (item) => {
      return this.templateResult(item, this.config.formatResult);
    };
    if (this.config.formatSelection) {
      this.actualConfig.templateSelection = this.config.formatSelection;
    }
    if (this.config.hideSearchBox) {
      this.actualConfig.minimumResultsForSearch = 'Infinity';
    }
  }

  ngOnInit() {
    // enables defaultToSingleValue and defaultToFirstValue for preloaded data in single selection mode
    if (!this.config.data) {
      return;
    }

    let dataLen = this.config.data.length;
    let value;
    if (this.config.defaultValue) {
      value = this.config.defaultValue;
    } else if (this.config.defaultToSingleValue && dataLen === 1 || this.config.defaultToFirstValue && dataLen > 0) {
      var item = _.find(this.config.data, function (current) {
        return !current.disabled;
      });

      if (item) {
        value = item.id;
      }
    }

    if (value) {
      this.$element.val(value).trigger('change');
    }
  }

  appendListeners(listeners) {
    for (let listener in listeners) {
      if (_.isFunction(listeners[listener])) {
        this.$element.on(listener, listeners[listener]);
      }
    }
  }

  /**
   * Wraps each result into a span holding its value for the result to be easily selectable
   * @param item
   * @param formatResult function to additionally format the result (passed via configuration)
   * @returns formatted result
   */
  templateResult(item, formatResult) {
    if (item.text) {
      return `<span data-value="${item.id}">${_.isFunction(formatResult) ? formatResult(item) : item.text}</span>`;
    }
  }

  /**
   * Adds two way binding between model and select2 via model watcher and select2 on change event.
   */
  bindToModel() {
    let ngModel = this.ngModel;
    if (ngModel) {
      this.$element.on('change', () => {
        let newValue = this.$element.val();
        let currentValue = this.ngModel.$viewValue;
        if (!Select.compareValues(newValue, currentValue)) {
          this.ngModel.$setViewValue(newValue);
        }
      });

      let viewValueChecker = (newValue) => {
        let currentValue = this.$element.val();
        if (!Select.compareValues(newValue, currentValue)) {
          this.$element.val(newValue);

          let select2Data = this.$element.data('select2');
          if (select2Data && select2Data.dataAdapter && select2Data.dataAdapter instanceof CustomDataAdapter) {
            select2Data.dataAdapter.selectValue(newValue).then(() => {
              this.$element.trigger('change');
            });
          } else {
            this.$element.trigger('change');
          }
        }
      };
      this.$scope.$watch(() => ngModel.$viewValue, viewValueChecker, true);
    }
  }

  /**
   * Compare two select2 values for equality. This method compare only strings or plain arrays with the same order.
   * @param firstValue
   * @param secondValue
   * @returns {boolean} true if both values are equal.
   */
  static compareValues(firstValue, secondValue) {
    if (firstValue === secondValue) {
      return true;
    } else if (firstValue instanceof Array && secondValue instanceof Array && firstValue.length === secondValue.length) {
      for (let i = 0; i < firstValue.length; i++) {
        if (firstValue[i] !== secondValue[i]) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Prevents auto opening of the select2 menu when an option is deselected.
   *
   * @param element - the select2 element
   */
  preventAutoOpening() {
    this.$element
      .on('select2:opening', this.handleMenuOpeningEvent.bind(this))
      .on('select2:unselecting', this.handleUnselectingEvent.bind(this));
  }

  handleMenuOpeningEvent(event) {
    if (this.keepMenuClosed) {
      this.keepMenuClosed = false;
      event.preventDefault();
    }
  }

  handleUnselectingEvent(event) {
    if (!event.params || !event.params.args || !event.params.args.originalEvent || !event.params.args.originalEvent.target) {
      return;
    }

    var target = event.params.args.originalEvent.target;
    if ($(target).is('.select2-selection__choice__remove')) {
      this.keepMenuClosed = true;
    }
  }

  ensureFocusOnTyping(element) {
    // TODO: This fix is needed because of https://github.com/select2/select2/issues/4398 and should be removed when select2 is updated to 4.1.0 !!!
    if (NavigatorAdapter.isInternetExplorer()) {
      element.on('DOMNodeInserted', () => {
        // Ensure the element is focused when typing.
        element.focus();
      });
    }
  }

  /**
   * Prevents deselecting items from the select based on the provided minimum length if it has a valid value
   * of above zero.
   *
   * @param length - the minimum amount of items to leave in the select
   */
  enforceMinimumSelectionLength(length) {
    if (!length || length < 1) {
      return;
    }

    var minimumSelectionLengthListener = (event) => {
      var currentSelection = this.$element.val();
      if (currentSelection.length === length) {
        event.preventDefault();
      }
    };

    this.appendListeners({'select2:unselecting': minimumSelectionLengthListener});
  }
}
