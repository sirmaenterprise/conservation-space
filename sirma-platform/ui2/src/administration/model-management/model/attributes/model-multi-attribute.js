import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelValidation} from 'administration/model-management/model/validation/model-validation';
import {ModelValue} from 'administration/model-management/model/model-value';
import {ModelList} from 'administration/model-management/model/model-list';

/**
 * Represents a model of an attribute extending the {@link ModelAttribute}
 * This attribute can contain a collection of multiple values. Each value is
 * mapped internally by the language it is representing.
 *
 * @author Svetlozar Iliev
 */
export class ModelMultiAttribute extends ModelAttribute {

  constructor(id, type = 'string') {
    super(id, type, null);
    this.values = new ModelList();
    this.validations = new ModelList();
  }

  //@Override
  setValue(value) {
    // make sure value is part of the values
    if (this.getValues().indexOf(value) == -1) {
      throw new TypeError('Base value should be a reference from values array');
    }
    this.validation = this.getValidationForValue(value);
    return super.setValue(value);
  }

  getValues() {
    return this.values.getModels();
  }

  getLanguages() {
    return this.getValues().map(value => value.getLanguage());
  }

  setValues(values) {
    this.values = values;
    return this;
  }

  getValueByLanguage(locale) {
    return this.values.getModel(locale);
  }

  getValidationForValue(value) {
    return this.validations.getModel(value.getLanguage());
  }

  addValue(value) {
    if (value instanceof ModelValue) {
      // append a new value to the list
      let mapper = (v) => v.getLanguage();
      this.values.insert(value, mapper);

      // link value and validation model
      let validation = new ModelValidation();
      this.validations.insert(validation, mapper(value));
    }
    return this;
  }

  //@Override
  isEmpty() {
    let values = this.getValues();
    return !values || values.every(value => value.isEmpty());
  }

  //@Override
  isDirty() {
    let dirty = super.isDirty();

    if (!dirty) {
      let values = this.getValues();
      dirty = values.some(value => value.isDirty());
    }
    return dirty;
  }

  //@Override
  isValid() {
    let valid = super.isValid();

    if (valid) {
      let validations = this.validations.getModels();
      valid = validations.every(model => model.isValid());
    }
    return valid;
  }

  //@Override
  setDirty(dirty) {
    super.setDirty(dirty);
    this.getValues().forEach(value => value.setDirty(dirty));
    return this;
  }

  //@Override
  restoreValue() {
    super.restoreValue();
    this.getValues().forEach(value => value.restoreValue());
    return this;
  }

  //@Override
  copyFrom(src) {
    super.copyFrom(src);
    src.getValues().forEach(v => {
      let newVal = new ModelValue();
      this.addValue(newVal.copyFrom(v));
    });
    if (src.getValue()) {
      let lang = src.getValue().getLanguage();
      this.setValue(this.getValueByLanguage(lang));
    }
    return this;
  }
}