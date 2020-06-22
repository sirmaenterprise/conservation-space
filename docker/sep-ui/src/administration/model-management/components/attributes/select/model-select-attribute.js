import {View, Component, Inject, NgScope, NgElement} from 'app/app';
import {TranslateService} from 'services/i18n/translate-service';
import {NavigatorAdapter} from 'adapters/navigator-adapter';
import {ModelGenericAttribute} from 'administration/model-management/components/attributes/model-generic-attribute';
import _ from 'lodash';

import 'select2';

import 'select2/css/select2.css!';
import './model-select-attribute.css!css';
import template from './model-select-attribute.html!text';

const SELECTOR = '.model-select-dropdown';

/**
 * Component rendering a dropdown. Internally this component is directly using the actual select2 implementation
 * Attribute model is provided through a component property and should be of type {@link ModelSingleAttribute}.
 *
 * @author B.Tonchev, svelikov, Svetlozar Iliev
 */
@Component({
  selector: 'model-select-attribute',
  properties: {
    'config': 'config',
    'context': 'context',
    'editable': 'editable',
    'attribute': 'attribute'
  },
  events: ['onChange']
})
@View({
  template
})
@Inject(NgScope, NgElement, TranslateService)
export class ModelSelectAttribute extends ModelGenericAttribute {

  constructor($scope, $element, translateService) {
    super({
      multiple: false,
      width: 'resolve',
      allowClear: false,
      appendMissing: false,
      placeholder: 'select.value.placeholder'
    });
    this.$scope = $scope;
    this.$element = $element;
    this.translateService = translateService;
  }

  ngOnInit() {
    this.initializeConfig();
    this.initializeSelect();
    this.initValueWatcher();
    this.initOptionsWatcher();
    this.registerSelectEvents();
  }

  initOptionsWatcher() {
    this.$scope.$watch(() => this.config.data, () => {
      if (this.shouldInsertMissingValue()) {
        this.appendCurrentValue();
      }
      this.initializeSelect();
      this.refreshValue();
    });
  }

  initValueWatcher() {
    this.$scope.$watch(() => this.getAttributeValue(), (newValue, oldValue) => {
      if (newValue !== oldValue) {
        if (this.shouldInsertMissingValue()) {
          this.appendCurrentValue();
          this.initializeSelect();
        }
        this.refreshValue();
      }
    });
  }

  registerSelectEvents() {
    // events which are triggered on manual selection or de-selection
    this.select.on('select2:select', () => this.changeSelection());
    this.select.on('select2:unselect', () => this.changeSelection());

    if (NavigatorAdapter.isSafari()) {
      this.select.on('select2:closing', (e) => {
        e.preventDefault();
        e.stopPropagation();
        setTimeout(() => this.select.select2().trigger('select2:close'), 0);
      });
    }
  }

  changeSelection() {
    this.$scope.$evalAsync(() => {
      let oldValue = this.getAttributeValue();
      this.setAttributeValue(this.select.val());
      this.onModelChange(oldValue);
    });
  }

  //@Override
  onModelChange(oldValue) {
    super.onModelChange(this.convertValue(oldValue));
  }

  initializeConfig() {
    if (this.config.placeholder) {
      this.config.placeholder = {
        id: this.isConvertibleToNumber() ? null : '',
        text: this.translate(this.config.placeholder)
      };
    }
    return this.config;
  }

  initializeSelect() {
    this.select = this.select || this.$element.find(SELECTOR);
    this.select && this.select.empty().select2(this.config);
  }

  appendCurrentValue() {
    let value = this.getAttributeValue();
    !this.hasValue(value) && this.insertValue(value);
  }

  refreshValue() {
    // refreshes the value without triggering actual select change event
    let value = this.getAttributeValue();
    if (this.config.commaSeparated) {
      value = value.replace(/\s/g, '').split(',');
    }
    this.select.val(value).trigger('change.select2');
  }

  convertValue(value) {
    return this.isConvertibleToNumber() ? this.getAsInteger(value) : this.getAsString(value);
  }

  insertValue(value) {
    this.config.data && this.config.data.unshift({id: value, text: value});
  }

  translate(label) {
    return this.translateService.translateInstant(label);
  }

  getAsInteger(value) {
    let parsed = parseInt(value, 10);
    return isNaN(parsed) ? null : parsed;
  }

  getAsString(value) {
    // treat the null values as empty strings
    return value !== null ? String(value) : '';
  }

  hasValue(value) {
    return !_.isNull(value) && !_.isUndefined(value) && !!_.find(this.config.data, v => v.id === value);
  }

  shouldInsertMissingValue() {
    return this.config.appendMissing;
  }

  isConvertibleToNumber() {
    let value = this.attribute.getValue().getOldValue();
    return value === null || Number.isInteger(value);
  }

  setAttributeValue(value) {
    let modelValue = this.attribute.getValue();
    modelValue.value = this.convertValue(value);
  }

  getAttributeValue() {
    let modelValue = this.attribute.getValue();
    return this.getAsString(modelValue.getValue());
  }

  ngOnDestroy() {
    // unbind all bound select events
    this.select.off('select2:select');
    this.select.off('select2:unselect');

    // finally destroy the entire select
    if (this.$element.data('select2')) {
      this.$element.select2('destroy');
    }

    // parent destructor
    super.ngOnDestroy();
  }
}