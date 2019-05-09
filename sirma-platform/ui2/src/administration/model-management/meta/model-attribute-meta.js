import {ModelMetaData} from 'administration/model-management/meta/model-meta';
import {ModelDescription} from 'administration/model-management/model/model-value';
import _ from 'lodash';

/**
 * Represents a specific attribute meta data extending from {@link ModelMetaData}.
 * Default value provided to the attribute meta class can be represented in multiple
 * ways. Provided Default value can either be an object or a simple primitive. The keys
 * of the object determine it's actual application and interpretation.
 *
 * 1) Primitive default value contains simple primitives such as strings or integers, the
 * value is applied for all available languages if the attribute is multi language.
 *
 * Simple primitive default value for all purposes:
 *
 * defaultValue = 'Some default Value'
 *
 * Primitive default value for multiple purposes:
 *
 * defaultValue = {
 *    missing: 'Default Value when missing',
 *    create: 'Default value when creating'
 * }
 *
 * 2) Object default value represents object with keys which can be either a purpose
 * or a language key. See {@link ModelAttributeMetaData#DEFAULT_VALUE_PURPOSE}
 *
 * Simple complex default value for all purposes:
 *
 * defaultValue = {
 *    en: 'Some Default Value',
 *    bg: '..................',
 *    de: '..................'
 * }
 *
 * Complex Default value for multiple purposes:
 *
 * defaultValue = {
 *    create: {
 *      en: 'Default Value for create',
 *      bg: '........................',
 *      de: '........................'
 *    },
 *    missing: {
 *      en: 'Default Value when missing',
 *      bg: '..........................',
 *      de: '..........................'
 *    }
 * }
 *
 * @author Svetlozar Iliev
 */
export class ModelAttributeMetaData extends ModelMetaData {

  constructor(id) {
    super(id);
    this.order = 0;
    this.type = null;
    this.defaultValue = null;
    this.tooltips = {};
    this.tooltip = null;
  }

  getType() {
    return this.type;
  }

  setType(type) {
    this.type = type;
    return this;
  }

  hasDefaultValue() {
    return hasSomePurpose(this.defaultValue) ? hasDefinedValues(this.defaultValue) : this.defaultValue;
  }

  getDefaultValue(purpose) {
    return hasSomePurpose(this.defaultValue) ? this.defaultValue[purpose] : this.defaultValue;
  }

  setDefaultValue(defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  getOrder() {
    return this.order;
  }

  setOrder(order) {
    this.order = order;
    return this;
  }

  setOptions(options = []) {
    this.options = options;
    return this;
  }

  getOptions() {
    return this.options || [];
  }

  getTooltipByLanguage(locale) {
    return this.tooltips[locale];
  }

  addTooltip(tooltip) {
    if (tooltip instanceof ModelDescription) {
      let lang = tooltip.getLanguage();
      this.tooltips[lang] = tooltip;
    }
    return this;
  }

  getTooltip() {
    return this.tooltip;
  }

  setTooltip(tooltip) {
    this.tooltip = tooltip;
    return this;
  }
}

ModelAttributeMetaData.DEFAULT_VALUE_PURPOSE = {
  CREATE: 'create',
  MISSING: 'missing'
};

function hasSomePurpose(inspectedValue) {
  let purposes = Object.values(ModelAttributeMetaData.DEFAULT_VALUE_PURPOSE);
  return _.isObject(inspectedValue) && purposes.some(p => !!inspectedValue[p]);
}

function hasDefinedValues(inspectedValue) {
  if (_.isObject(inspectedValue)) {
    let keys = Object.keys(inspectedValue);
    let callback = (val, key) => hasDefinedValues(val[key]);
    return keys.some(key => callback(inspectedValue, key));
  }
  return !!inspectedValue;
}