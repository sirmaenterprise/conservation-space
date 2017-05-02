import {AfterFormValidationEvent} from 'form-builder/validation/after-form-validation-event';
import {ValidationService} from 'form-builder/validation/validation-service';
import {Mandatory} from 'form-builder/validation/mandatory/mandatory';
import {PromiseStub} from 'test/promise-stub';
import {InstanceModel, InstanceModelProperty} from 'models/instance-model';
import {DefinitionModel} from 'models/definition-model';

describe('ValidationService', function () {

  let eventbus = {
    publish: function() {}
  };

  let translateService = { translateInstant: sinon.stub() };

  let configuration = {
    get: () => { return true; }
  };

  let logger = {
    error: () => {}
  };

  function setupPluginsService(validators) {
    let pluginsService = {};
    pluginsService.loadPluginServiceModules = () => {
      return PromiseStub.resolve(validators);
    };
    return pluginsService;
  }

  describe('shouldExecuteValidator()', () => {
    it('should return true if a validator does not provide execution property and execution argument not passed to validate', () => {
      expect(ValidationService.shouldExecuteValidator({}, null)).to.be.true;
    });

    it('should return true if a validator does not provide execution property', () => {
      expect(ValidationService.shouldExecuteValidator({}, 'beforeRender')).to.be.true;
    });

    it('should return true if a validator provides execution property that is equal to execution argument passed to validate', () => {
      expect(ValidationService.shouldExecuteValidator({
        execution: 'beforeRender'
      }, 'beforeRender')).to.be.true;
    });

    it('should return true if a validator have execution property but execution argument is not passed to validate', () => {
      expect(ValidationService.shouldExecuteValidator({
        execution: 'beforeSave'
      }, null)).to.be.true;
    });

    it('should return false if a validator have execution property and it does not match the execution argument passed to validate', () => {
      expect(ValidationService.shouldExecuteValidator({
        execution: 'beforeSave'
      }, 'beforeRender')).to.be.false;
    });
  });

  describe('updateValidationMessages', function () {
    it('should set validation message if a validator fails', function () {
      let isValid = false;
      //messages is an instance of ObservableArray
      let fieldValidationModel = new InstanceModelProperty({
        messages: []
      });
      let validatorDefintion = {
        'id': 'equal',
        'level': 'error',
        'message': 'Validator message'
      };
      ValidationService.setMessages(isValid, fieldValidationModel, validatorDefintion, translateService);
      expect(fieldValidationModel.messages[0]).to.eql({
        'id': 'equal',
        'level': 'error',
        'message': 'Validator message'
      });
    });

    it('should remove validation message if a validator succeeds', function () {
      let isValid = true;
      let fieldValidationModel = new InstanceModelProperty({
        messages: [{
          id: 'equal'
        }]
      });
      let validatorDefintion = {
        'id': 'equal',
        'level': 'error',
        'message': 'Validator message'
      };
      ValidationService.setMessages(isValid, fieldValidationModel, validatorDefintion, translateService);
      expect(fieldValidationModel.messages.length).to.equal(0);
    });
  });

  describe('setValidity', function () {
    it('should set the valid status on fields validation model', function () {
      let fieldName = 'textfield1';
      let validationModel = {};
      validationModel[fieldName] = {};
      ValidationService.setValidity(fieldName, validationModel, true);
      expect(validationModel[fieldName].valid).to.be.true;
    });
  });

  describe('validate', function () {
    let id = null;
    let execution = true;
    let formControl = null;

    it('should return true if all mandatory fields have values', function () {
      let pluginsService = setupPluginsService({
        'mandatory': new Mandatory()
      });
      let service = new ValidationService(pluginsService, {}, translateService, eventbus, logger, configuration);
      service.init();
      let isValid = service.validate(validationModelMandatoryFieldWithValue, viewModelMandatoryFields, id, execution, formControl);
      expect(isValid).to.be.true;
    });

    it('should return false if a mandatory field have no value', function () {
      let pluginsService = setupPluginsService({
        'mandatory': new Mandatory()
      });
      let service = new ValidationService(pluginsService, {}, translateService, eventbus, logger, configuration);
      service.init();
      let isValid = service.validate(validationModel, new DefinitionModel(viewModelMandatoryFields).flatModelMap, id, execution, formControl);
      expect(isValid).to.be.false;
    });

    it('should return false if a mandatory field has value but failed validator', function () {
      let equalValidator = {
        validate: function () {
          return false
        }
      };
      let pluginsService = setupPluginsService({
        'equal': equalValidator
      });
      let service = new ValidationService(pluginsService, {}, translateService, eventbus, logger, configuration);
      service.init();
      let isValid = service.validate(validationModel, new DefinitionModel(viewModelMandatoryFieldsWithValidators).flatModelMap, id, execution, formControl);
      expect(isValid).to.be.false;
    });

    it('should invoke every registered validator for every field in the model', function () {
      let equalValidator = {
        validate: function () {
          return true
        }
      };
      let spyEqualValidator = sinon.spy(equalValidator, 'validate');
      let notEqualValidator = {
        validate: function () {
          return true
        }
      };
      let spyNotEqualValidator = sinon.spy(notEqualValidator, 'validate');
      let pluginsService = setupPluginsService({
        'equal': equalValidator,
        'notEqual': notEqualValidator
      });
      let service = new ValidationService(pluginsService, {}, translateService, eventbus, logger, configuration);
      service.init();
      let isValid = service.validate(validationModel, new DefinitionModel(viewModel).flatModelMap, id, execution, formControl);
      expect(spyEqualValidator.callCount).to.equal(1);
      expect(spyNotEqualValidator.callCount).to.equal(1);
    });

    it('should return true if there are not failing validators', function () {
      let equalValidator = {
        validate: function () {
          return true
        }
      };
      let notEqualValidator = {
        validate: function () {
          return true
        }
      };
      let pluginsService = setupPluginsService({
        'equal': equalValidator,
        'notEqual': notEqualValidator
      });
      let service = new ValidationService(pluginsService, {}, translateService, eventbus, logger, configuration);
      service.init();
      let isValid = service.validate(validationModel, new DefinitionModel(viewModel).flatModelMap, id, execution, formControl);
      expect(isValid).to.be.true;
    });

    it('should return false if there are failing validators', function () {
      let equalValidator = {
        validate: function () {
          return false
        }
      };
      let notEqualValidator = {
        validate: function () {
          return true
        }
      };
      let pluginsService = setupPluginsService({
        'equal': equalValidator,
        'notEqual': notEqualValidator
      });
      let service = new ValidationService(pluginsService, {}, translateService, eventbus, logger, configuration);
      service.init();
      let isValid = service.validate(validationModel, new DefinitionModel(viewModel).flatModelMap, id, execution, formControl);
      expect(isValid).to.be.false;
    });

    it('should update the validity flag in validation model when validation succeeds', function () {
      let equalValidator = {
        validate: function () {
          return true
        }
      };
      let notEqualValidator = {
        validate: function () {
          return true
        }
      };
      let pluginsService = setupPluginsService({
        'equal': equalValidator,
        'notEqual': notEqualValidator
      });
      let service = new ValidationService(pluginsService, {}, translateService, eventbus, logger, configuration);
      service.init();
      let isValid = service.validate(validationModel, new DefinitionModel(viewModel).flatModelMap, id, execution, formControl);
      expect(validationModel.isValid).to.be.true;
    });

    it('should update the validity flag in validation model when validation fails', function () {
      let equalValidator = {
        validate: function () {
          return false
        }
      };
      let notEqualValidator = {
        validate: function () {
          return true
        }
      };
      let pluginsService = setupPluginsService({
        'equal': equalValidator,
        'notEqual': notEqualValidator
      });
      let service = new ValidationService(pluginsService, {}, translateService, eventbus, logger, configuration);
      service.init();
      let isValid = service.validate(validationModel, new DefinitionModel(viewModel).flatModelMap, id, execution, formControl);
      expect(validationModel.isValid).to.be.false;
    });

    it('should return true if there are no registered validators', function () {
      let pluginsService = setupPluginsService({});
      let service = new ValidationService(pluginsService, {}, translateService, eventbus, logger, configuration);
      service.init();
      let isValid = service.validate(validationModel, new DefinitionModel(viewModelNoValidators).flatModelMap, id, execution, formControl);
      expect(isValid).to.be.true;
    });

    it('should fire event after form validation', function() {
      let pluginsService = setupPluginsService({});
      let spyPublish = sinon.spy(eventbus, 'publish');
      let service = new ValidationService(pluginsService, {}, translateService, eventbus, logger, configuration);
      service.validate(validationModel, viewModelNoValidators, 'emf:123456');
      expect(spyPublish.callCount).to.equal(1);
      expect(spyPublish.getCall(0).args[0].args[0]).to.deep.equal({
        isValid: true,
        id: 'emf:123456',
        validationModel: validationModel,
        viewModel: viewModelNoValidators
      });
    })

  });

  let viewModel = {
    fields: [
      {
        identifier: 'testfield1',
        validators: [
          {
            id: 'equal'
          }
        ]
      },
      {
        identifier: 'region1',
        fields: [
          {
            identifier: 'testfield3',
            validators: [
              {
                id: 'notEqual'
              }
            ]
          },
          {
            identifier: 'testfield4'
          }
        ]
      },
      {
        identifier: 'testfield5'
      },
      {
        identifier: 'testfield6',
        displayType: 'SYSTEM'
      },
      {
        identifier: 'testfield7',
        displayType: 'HIDDEN'
      },
      {
        identifier: 'testfield8',
        displayType: 'READ_ONLY'
      }
    ]
  };

  let flatViewModel = {
    testfield1: {
      identifier: 'testfield1',
      validators: [{
        id: 'equal'
      }]
    },
    region1: {
      identifier: 'region1',
      fields: [{
        identifier: 'testfield3',
        validators: [
          {
            id: 'notEqual'
          }
        ]
      },
        {
          identifier: 'testfield4'
        }]
    },
    testfield3: {
      identifier: 'testfield3',
      validators: [{
        id: 'notEqual'
      }]
    },
    testfield4: {
      identifier: 'testfield4'
    },
    testfield5: {
      identifier: 'testfield5'
    }
  };

  let viewModelNoValidators = {
    fields: [
      {
        identifier: 'testfield1',
        validators: []
      },
      {
        identifier: 'region1',
        fields: [
          {
            identifier: 'testfield3',
            validators: []
          },
          {
            identifier: 'testfield4'
          }
        ]
      },
      {
        identifier: 'testfield5'
      }
    ]
  };

  let viewModelMandatoryFields = {
    fields: [
      {
        identifier: 'testfield1',
        isMandatory: true,
        displayType: 'EDITABLE',
        validators: [{
          id: "mandatory",
          level: "error",
          message: "The field is mandatory!"
        }]
      },
      {
        identifier: 'region1',
        fields: [
          {
            identifier: 'testfield3',
            displayType: 'EDITABLE',
            validators: []
          },
          {
            identifier: 'testfield4',
            displayType: 'EDITABLE'
          }
        ]
      },
      {
        identifier: 'testfield5',
        displayType: 'EDITABLE'
      }
    ]
  };

  let viewModelMandatoryFieldsWithValidators = {
    fields: [
      {
        identifier: 'testfield1',
        isMandatory: true,
        validators: [{
          id: 'equal'
        }]
      },
      {
        identifier: 'region1',
        fields: [
          {
            identifier: 'testfield3',
            validators: []
          },
          {
            identifier: 'testfield4'
          }
        ]
      },
      {
        identifier: 'testfield5'
      }
    ]
  };
// messages is instance of InstanceModel
  var validationModel = new InstanceModel({
    'testfield1': {messages: []},
    'testfield3': {messages: []},
    'testfield5': {messages: []}
  });

  var validationModelMandatoryFieldWithValue = new InstanceModel({
    'testfield1': {messages: [], value: '123'},
    'testfield3': {messages: []},
    'testfield4': {messages: []},
    'testfield5': {messages: []}
  });
});
