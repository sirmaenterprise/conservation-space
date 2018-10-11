/**
 * Represents a model of a pair. Model Pair provides
 * information about the current internal language it is using
 * and the value which is associated with the given language
 *
 * @author Svetlozar Iliev
 */
export class ModelValueLanguagePair {

  constructor(language, value = '') {
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
}

/**
 * Markup class which extends from the {@link ModelValueLanguagePair}
 *
 * @author Svetlozar Iliev
 */
export class ModelDescription extends ModelValueLanguagePair {
}

/**
 * Markup class which extends from the {@link ModelValueLanguagePair}
 *
 * @author Svetlozar Iliev
 */
export class ModelValue extends ModelValueLanguagePair {
}