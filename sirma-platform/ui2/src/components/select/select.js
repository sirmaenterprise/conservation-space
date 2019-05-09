import {View, Component, Inject, NgElement, NgScope, NgTimeout} from 'app/app';
import {CustomDataAdapter} from 'components/select/adapters/custom-data-adapter';
import {ReusableComponent} from 'components/reusable-component';
import {NavigatorAdapter} from 'adapters/navigator-adapter';
import {JsonUtil} from 'common/json-util';
import {HtmlUtil} from 'common/html-util';
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

    this.enableReloadOnDataChange();
  }

  initSelect() {
    this.initSelectPlugin(this.actualConfig);

    // Invoked before attaching any listeners to avoid potential problems
    this.autoExpandDropdownMenu();

    // manually destroy the element to prevent memory leak
    this.$element.on('remove', () => {
      if (this.$element.data('select2')) {
        this.$element.select2('destroy');
      }
    });

    // Prevent page to scroll when option is selected under Safari.
    // This looks like problem in select2 possible related to https://github.com/select2/select2/issues/5022
    if (NavigatorAdapter.isSafari()) {
      this.$element.on('select2:closing', (e) => {
        e.preventDefault();
        e.stopPropagation();
        this.$timeout(() => this.$element.select2().trigger('select2:close'), 0);
      });
    }

    // Append listeners
    if (this.config.listeners) {
      this.appendListeners(this.config.listeners);
    }
    this.bindToModel();
    this.preventAutoOpening();
    this.ensureFocusOnTyping(this.$element);
    this.enforceMinimumSelectionLength(this.config.minimumSelectionLength);
  }

  initSelectPlugin(config) {
    this.$element.select2(config);
  }

  createActualConfig() {
    this.actualConfig = _.pick(this.config, [
      'tags', 'mapper', 'width', 'multiple', 'placeholder', 'allowClear', 'dropdownAutoWidth',
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
        return {results};
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

    if (this.config.templateResult) {
      this.actualConfig.templateResult = this.config.templateResult;
    } else {
      this.actualConfig.templateResult = (item) => {
        return this.templateResult(item, this.config.formatResult);
      };
    }

    if (this.config.formatSelection) {
      this.actualConfig.templateSelection = this.config.formatSelection;
    } else {
      this.actualConfig.templateSelection = (item) => {
        return HtmlUtil.escapeHtml(item.text);
      };
    }

    if (this.config.hideSearchBox) {
      this.actualConfig.minimumResultsForSearch = 'Infinity';
    }
  }

  ngOnInit() {
    this.applyDefaultValue();
  }

  /**
   * Enables defaultToSingleValue and defaultToFirstValue for preloaded data in single selection mode
   */
  applyDefaultValue() {
    if (!this.config.data) {
      return;
    }

    let dataLen = this.config.data.length;
    let value;
    if (this.config.defaultValue) {
      value = this.config.defaultValue;
    } else if (this.config.defaultToSingleValue && dataLen === 1 || this.config.defaultToFirstValue && dataLen > 0) {
      let item = _.find(this.config.data, function (current) {
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
      let text = item.text;
      if (_.isFunction(formatResult)) {
        text = formatResult(item);
      } else {
        text = HtmlUtil.escapeHtml(text);
      }
      return `<span data-value="${item.id}">${text}</span>`;
    }
  }

  enableReloadOnDataChange() {
    if (this.config.reloadOnDataChange) {
      this.$scope.$watch(() => {
        return this.config.data;
      }, () => {
        this.createActualConfig();
        this.$element.empty();
        this.initSelect();
        this.applyDefaultValue();
      });
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
        if (this.config.convertToNumber) {
          let newValueToNumber = parseInt(newValue);
          newValue = isNaN(newValueToNumber) ? null : newValueToNumber;
        }
        else if (this.config.convertToString) {
          let newValueToString = String(newValue);
          newValue = _.isUndefined(newValue) || _.isNull(newValue) ? '' : newValueToString;
        }
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

    let target = event.params.args.originalEvent.target;
    if ($(target).is('.select2-selection__choice__remove')) {
      this.keepMenuClosed = true;
    }
  }

  ensureFocusOnTyping(element) {
    // TODO: This fix is needed because of https://github.com/select2/select2/issues/4398 and should be removed when select2 is updated to 4.1.0 !!!
    if (NavigatorAdapter.isInternetExplorer() && this.config.tags !== undefined) {
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

    let minimumSelectionLengthListener = (event) => {
      let currentSelection = this.$element.val();
      if (currentSelection.length === length) {
        event.preventDefault();
      }
    };

    this.appendListeners({'select2:unselecting': minimumSelectionLengthListener});
  }

  // Workaround for https://github.com/select2/select2/issues/4678
  autoExpandDropdownMenu() {
    if (this.config.dropdownAutoWidth && NavigatorAdapter.isSafari()) {
      // Safari is not auto expanding the dropdown menu on the first open and needs to be forced.
      this.$element.select2('open');
      this.$element.select2('close');
    }
  }

  ngOnDestroy() {
    this.$element.remove();
  }

  isDisabled() {
    if (this.config.isDisabled) {
      return !!this.config.isDisabled();
    }
    return this.config.disabled;
  }
}
