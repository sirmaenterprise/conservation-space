var util = {
  buildFormObject: function (fieldName, viewValue, formObject) {
    var form = formObject || {};
    form[fieldName] = {
      '$viewValue': viewValue
    };
    return form;
  },

  buildValidationModel: function(fieldName, value, modelObject) {
    var model = modelObject || {};
    model[fieldName] = {
      value: value
    };
    return model;
  },

  buildFlatViewModel: function(fieldName, property, value) {
    var model = {};
    model[fieldName] = {};
    model[fieldName][property] = value;
    return model;
  },

  buildValidatorDefinition: function (expectedValue) {
    var definition = {};
    var context = {};
    if(typeof expectedValue === 'object') {
      context = expectedValue;
    } else {
      context.value = expectedValue;
    }
    definition.context = context;
    return definition;
  }
};
