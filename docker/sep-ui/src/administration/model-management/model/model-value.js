import _ from 'lodash';

/**
 * Represents a model description. The description provides a pair
 * about the current internal language it is using and the value
 * which is associated with the given language
 *
 * @author Svetlozar Iliev
 */
export class ModelDescription {

  constructor(language, value) {
    this._value = value;
    this._language = language;
  }

  getLanguage() {
    return this._language;
  }

  get language() {
    return this.getLanguage();
  }

  setLanguage(language) {
    this._language = language;
    return this;
  }

  set language(language) {
    this.setLanguage(language);
  }

  getValue() {
    return this._value;
  }

  getValueEscaped() {
    if (_.isString(this.getValue())) {
      // escape special symbols from the value currently stored value
      return this.getValue().replace(/['".*+?^${}()|[\]\\]/g, '\\$&');
    }
    // nothing to handle
    return this.getValue();
  }

  get value() {
    return this.getValue();
  }

  setValue(value) {
    this._value = value;
    return this;
  }

  set value(value) {
    this.setValue(value);
  }

  isEmpty() {
    let actualValue = this.getValue();
    return _.isUndefined(actualValue) || _.isNull(actualValue) || ((_.isObject(actualValue)
      || _.isString(actualValue)) && _.isEmpty(actualValue));
  }

  copyFrom(src) {
    this._value = src._value;
    this._language = src._language;
    return this;
  }

  equals(modelValue) {
    return _.isEqual(this.getValue(), modelValue.getValue());
  }
}

/**
 * Class which extends from the {@link ModelDescription} and represents a
 * model of a value which in addition to the language and value associated
 * with that language holds the previous value associated with that language
 *
 * @author Svetlozar Iliev
 */
export class ModelValue extends ModelDescription {

  constructor(language, value) {
    super(language, value);
    this._oldValue = value;
  }

  isDirty() {
    return !_.isEqual(this.getValue(), this.getOldValue());
  }

  setDirty(dirty) {
    //TODO: think about that, is null a proper value anyway ?
    return this.setOldValue(!dirty ? this.getValue() : null);
  }

  getOldValue() {
    return this._oldValue;
  }

  get oldValue() {
    return this.getOldValue();
  }

  setOldValue(oldValue) {
    this._oldValue = oldValue;
    return this;
  }

  set oldValue(oldValue) {
    this.setOldValue(oldValue);
  }

  restoreValue() {
    this.setValue(this.getOldValue());
  }

  copyFrom(src) {
    super.copyFrom(src);
    this._oldValue = src._oldValue;
    return this;
  }
}