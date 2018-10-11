/**
 * Created by svelikov on 6/26/18.
 */

export function mockFormWrapper() {
  let formWrapper = {
    getViewModel: sinon.stub(),
    fieldsMap: {},
    formConfig: {
      models: {
        validationModel: {},
        viewModel: {}
      }
    },
    objectDataForm: {},
    config: {}
  };

  return {
    setViewModel: function(viewModel) {
      formWrapper.formConfig.models.viewModel = viewModel;
      return this;
    },
    setValidationModel: function(validationModel) {
      formWrapper.formConfig.models.validationModel = validationModel;
      return this;
    },
    setFieldsMap: function(fieldsMap) {
      formWrapper.fieldsMap = fieldsMap;
      formWrapper.getViewModel.returns(fieldsMap);
      return this;
    },
    setObjectDataForm: function(objectDataForm) {
      formWrapper.objectDataForm = objectDataForm;
      return this;
    },
    setConfig: function(config) {
      formWrapper.config = config;
      return this;
    },
    get: function() {
      return formWrapper;
    }
  };
}