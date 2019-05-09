import _ from 'lodash';

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

  static isBoolean(type) {
    return type === ModelAttributeTypes.SINGLE_VALUE.MODEL_BOOLEAN_TYPE;
  }

  static isTypeOfTypes(type, types) {
    return Object.values(types).indexOf(type) > -1;
  }

  static getDefaultValue(type) {
    if (ModelAttributeTypes.isSingleValued(type)) {
      // resolve default value or fallback to empty string
      let value = ModelAttributeTypes.DEFAULT_VALUE[type];
      let defaults = ModelAttributeTypes.DEFAULT_SINGLE_VALUE;
      return !_.isUndefined(value) ? value : defaults;
    } else if (ModelAttributeTypes.isMultiValued(type)) {
      // simply treat all multi values as empty maps
      return ModelAttributeTypes.DEFAULT_MULTI_VALUE;
    }
    return null;
  }
}

ModelAttributeTypes.DEFAULT_MULTI_VALUE = {};
ModelAttributeTypes.DEFAULT_SINGLE_VALUE = '';

ModelAttributeTypes.SINGLE_VALUE = {
  // generic single type values
  MODEL_OPTION_TYPE: 'option',
  MODEL_STRING_TYPE: 'string',
  MODEL_INTEGER_TYPE: 'integer',
  MODEL_BOOLEAN_TYPE: 'boolean',
  MODEL_IDENTIFIER_TYPE: 'identifier',
  // semantic property value types
  MODEL_RANGE_TYPE: 'range',
  MODEL_DOMAIN_TYPE: 'domain',
  MODEL_PROPERTY_TYPE: 'propertyType',
  // definition field value types
  MODEL_URI_TYPE: 'uri',
  MODEL_VALUE_TYPE: 'value',
  MODEL_CODE_LIST_TYPE: 'codeList',
  MODEL_DISPLAY_TYPE: 'displayType',
  MODEL_CODE_VALUE_TYPE: 'codeValue',
  MODEL_TYPE_OPTION_TYPE: 'typeOption'
};

ModelAttributeTypes.MULTI_VALUE = {
  MODEL_LABEL_TYPE: 'label',
  MODEL_HEADER_TYPE: 'header',
  MODEL_MULTI_LANG_STRING_TYPE: 'multiLangString'
};

ModelAttributeTypes.DEFAULT_VALUE = {
  [ModelAttributeTypes.SINGLE_VALUE.MODEL_INTEGER_TYPE]: null,
  [ModelAttributeTypes.SINGLE_VALUE.MODEL_BOOLEAN_TYPE]: false,
  [ModelAttributeTypes.SINGLE_VALUE.MODEL_CODE_LIST_TYPE]: null
};