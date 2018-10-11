import {ModelValue} from 'administration/model-management/model/model-value';

/**
 * Represents a model of an attribute extending the {@link ModelBase} with
 * additional properties such as type, value and default value
 *
 * @author Svetlozar Iliev
 */
export class ModelAttribute {

  constructor(id, type, value) {
    this._id = id;
    this._type = type;
    this._value = value;

    // "Transient" fields
    this._metaData = null;
    this._validation = null;
  }

  getParent() {
    return this.parent;
  }

  setParent(parent) {
    this.parent = parent;
    return this;
  }

  getId() {
    return this._id;
  }

  setId(id) {
    this._id = id;
    return this;
  }

  getType() {
    return this._type;
  }

  get type() {
    return this.getType();
  }

  setType(type) {
    this._type = type;
    return this;
  }

  set type(type) {
    this.setType(type);
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

  getMetaData() {
    return this._metaData;
  }

  get metaData() {
    return this.getMetaData();
  }

  setMetaData(metaData) {
    this._metaData = metaData;
    return this;
  }

  set metaData(metaData) {
    this.setMetaData(metaData);
  }

  getValidation() {
    return this.validation;
  }

  setValidation(validation) {
    this.validation = validation;
    return this;
  }
}

/**
 * Represents a model of an attribute extending the {@link ModelAttribute}
 * This attribute is strictly restricted to supporting only a single value.
 * Such value is typically a standard primitive such as a string or a boolean
 *
 * @author Svetlozar Iliev
 */
export class ModelSingleAttribute extends ModelAttribute {

  constructor(id, type = 'string', value = null) {
    super(id, type, value);
  }
}

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
    this.values = {};
  }

  getValues() {
    return this.values;
  }

  setValues(values) {
    this.values = values;
    return this;
  }

  getValueByLanguage(locale) {
    return this.values[locale];
  }

  addValue(value) {
    if (value instanceof ModelValue) {
      let lang = value.getLanguage();
      this.values[lang] = value;
    }
    return this;
  }
}

/**
 * Represents a collection of constants which hold all different attribute types
 * Attribute types can be multi valued or single valued. Depending on the actual
 * type of the attribute behaviour can differ
 *
 * This class also provides functionality for validating and operating with the
 * different attribute types
 *
 * @author Svetlozar Iliev
 */
export class ModelAttributeTypes {

  static isMultiValued(type) {
    return ModelAttributeTypes.isTypeOfTypes(type, ModelAttributeTypes.MULTI_VALUE);
  }

  static isSingleValued(type) {
    return ModelAttributeTypes.isTypeOfTypes(type, ModelAttributeTypes.SINGLE_VALUE);
  }

  static isLabel(type) {
    return type === ModelAttributeTypes.MULTI_VALUE.MODEL_LABEL_TYPE;
  }

  static isIdentifier(type) {
    return type === ModelAttributeTypes.SINGLE_VALUE.MODEL_IDENTIFIER_TYPE;
  }

  static isTypeOfTypes(type, types) {
    return Object.values(types).indexOf(type) > -1;
  }
}

ModelAttributeTypes.SINGLE_VALUE = {
  MODEL_URI_TYPE: 'uri',
  MODEL_STRING_TYPE: 'string',
  MODEL_INTEGER_TYPE: 'integer',
  MODEL_BOOLEAN_TYPE: 'boolean',
  MODEL_DISPLAY_TYPE: 'displayType',
  MODEL_IDENTIFIER_TYPE: 'identifier'
};

ModelAttributeTypes.MULTI_VALUE = {
  MODEL_LABEL_TYPE: 'label',
  MODEL_CODE_LIST_TYPE: 'codeList',
  MODEL_MULTI_LANG_STRING_TYPE: 'multiLangString'
};