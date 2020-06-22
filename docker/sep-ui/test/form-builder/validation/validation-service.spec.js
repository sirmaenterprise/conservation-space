import {ValidationService} from 'form-builder/validation/validation-service';
import {Mandatory} from 'form-builder/validation/mandatory/mandatory';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';
import {PluginsService} from 'services/plugin/plugins-service';
import {NotificationService} from 'services/notification/notification-service';
import {TranslateService} from 'services/i18n/translate-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {Logger} from 'services/logging/logger';
import {Configuration} from 'common/application-config';
import {ValidationModelBuilder} from 'test/form-builder/validation-model-builder';
import {InstanceObject} from 'models/instance-object';
import {ViewModelBuilder} from 'test/form-builder/view-model-builder';
import {InstanceModelProperty} from 'models/instance-model';

describe('ValidationService', function () {

  const INSTANCE_ID_ONE = 'emf:002';
  const INSTANCE_ID_TWO = 'emf:001';

  let translateService;

  beforeEach(() => {
    translateService = stub(TranslateService);
  });

  describe('validate', function () {

    const HAVE_ASYNC_INVALIDATION = false;

    let id = null;
    let execution = true;
    let formControl = null;

    let validationService;
    let pluginsService;
    let notificationService;
    let translateService;
    let eventbus;
    let logger;
    let configuration;

    beforeEach(() => {
      pluginsService = stub(PluginsService);
      notificationService = stub(NotificationService);
      translateService = stub(TranslateService);
      eventbus = stub(Eventbus);
      logger = stub(Logger);
      configuration = stub(Configuration);

      validationService = new ValidationService(pluginsService, notificationService, translateService, eventbus, logger, configuration, PromiseStub);
    });

    it('should return true if all mandatory fields have values', function () {
      let instanceObject = createInstanceObjectWithMandatoryField(INSTANCE_ID_ONE, '123');
      setupPluginService();

      validate(validationService, instanceObject).then((isValid) => {
        expect(isValid).to.be.true;
      });
    });

    it('should return false if a mandatory field has no value', function () {
      let instanceObject = createInstanceObjectWithMandatoryField(INSTANCE_ID_ONE);
      setupPluginService();

      validate(validationService, instanceObject).then((isValid) => {
        expect(isValid).to.be.false;
      });
    });

    it('should return true if async validator return true', () => {
      let instanceObject = createInstanceObjectWithAsyncFieldValidation(INSTANCE_ID_TWO);
      setupPluginService();

      validate(validationService, instanceObject).then((isValid) => {
        expect(isValid).to.be.true;
      });
    });

    it('should return false if async validator return false', () => {
      let instanceObject = createInstanceObjectWithAsyncFieldValidation(INSTANCE_ID_TWO);
      setupPluginService(HAVE_ASYNC_INVALIDATION);

      validate(validationService, instanceObject).then((isValid) => {
        expect(isValid).to.be.false;
      });
    });

    it('should not mark instance object as invalid when it have not id', () => {
      // Given:
      // instance without id is created.
      let instanceObject = createInstanceObjectWithAsyncFieldValidation();
      setupPluginService();

      // When execute validation.
      validateAll(validationService, [instanceObject]).then((invalidObjects) => {
        // Then:
        // invalidObjects have to be empty
        expect(invalidObjects).to.deep.equal({});
      });

    });

    it('it should all instance are invalid', () => {
      // Given:
      // instance with not populated mandatory field is created.
      let instanceObjectWithInvalidMandatoryField = createInstanceObjectWithMandatoryField(INSTANCE_ID_ONE);

      // instance with unique field is created.
      let instanceObjectWithInvalidUniqueField = createInstanceObjectWithAsyncFieldValidation(INSTANCE_ID_TWO);
      // setup plugin service to return that unique field is invalid.
      setupPluginService(HAVE_ASYNC_INVALIDATION);

      // When execute validation.
      validateAll(validationService, [instanceObjectWithInvalidMandatoryField, instanceObjectWithInvalidUniqueField]).then((invalidObjects) => {
        // Then:
        // both instance have to be valid.
        expect(invalidObjects[instanceObjectWithInvalidMandatoryField.getId()].isValid).to.be.false;
        expect(invalidObjects[instanceObjectWithInvalidUniqueField.getId()].isValid).to.be.false;
      });
    });

    it('it should instance with mandatory field be invalid', () => {
      // Given:
      // instance with not populated mandatory field is created.
      let instanceObjectWithInvalidMandatoryField = createInstanceObjectWithMandatoryField(INSTANCE_ID_ONE);

      // instance with unique field is created.
      let instanceObjectWithInvalidUniqueField = createInstanceObjectWithAsyncFieldValidation(INSTANCE_ID_TWO);
      // setup plugin service to return that unique field is valid.
      setupPluginService();

      // When execute validation.
      validateAll(validationService, [instanceObjectWithInvalidMandatoryField, instanceObjectWithInvalidUniqueField]).then((invalidObjects) => {
        // Then:
        // only instance with unique field have to be valid.
        expect(invalidObjects[instanceObjectWithInvalidMandatoryField.getId()].isValid).to.be.false;
        expect(invalidObjects[instanceObjectWithInvalidUniqueField.getId()].isValid).to.be.true;
      });
    });

    it('it should instance with unique field be invalid', () => {
      // Given:
      // instance with populated mandatory field is created.
      let instanceWithMandatoryField = createInstanceObjectWithMandatoryField(INSTANCE_ID_ONE, '123');

      // instance with unique field is created.
      let instanceObjectWithInvalidUniqueField = createInstanceObjectWithAsyncFieldValidation(INSTANCE_ID_TWO);
      // setup plugin service to return that unique field is invalid.
      setupPluginService(HAVE_ASYNC_INVALIDATION);

      // When execute validation.
      validateAll(validationService, [instanceWithMandatoryField, instanceObjectWithInvalidUniqueField]).then((invalidObjects) => {
        // Then:
        // only instance with mandatory field have to be valid.
        expect(invalidObjects[instanceWithMandatoryField.getId()].isValid).to.be.true;
        expect(invalidObjects[instanceObjectWithInvalidUniqueField.getId()].isValid).to.be.false;
      });
    });

    it('it all instances have to be valid', () => {
      // Given:
      // instance with populated mandatory field is created.
      let instanceWithMandatoryField = createInstanceObjectWithMandatoryField(INSTANCE_ID_ONE, '123');

      // instance with unique field is created.
      let instanceObjectWithUniqueField = createInstanceObjectWithAsyncFieldValidation(INSTANCE_ID_TWO);
      // setup plugin service to return that unique field is valid.
      setupPluginService();

      // When execute validation.
      validateAll(validationService, [instanceWithMandatoryField, instanceObjectWithUniqueField]).then((invalidObjects) => {
        // Then:
        // both instance object have to be valid.
        expect(invalidObjects[instanceWithMandatoryField.getId()].isValid).to.be.true;
        expect(invalidObjects[instanceObjectWithUniqueField.getId()].isValid).to.be.true;
      });
    });

    function setupPluginService(uniqueResult = true) {
      let validators = {
        mandatory: new Mandatory(),
        unique: {validate(){return PromiseStub.resolve(uniqueResult);}}
      };

      pluginsService.loadPluginServiceModules.returns(PromiseStub.resolve(validators));

      pluginsService.getPluginDefinitions.returns({
        mandatory: {
          'name': 'mandatory',
          'level': 'error',
          'message': 'Validator message'
        },
        unique: {
          'name': 'unique',
          'level': 'error',
          'async': true,
          'message': 'Validator message'
        }
      });
      return pluginsService;
    }

    function createInstanceObjectWithMandatoryField(id, mandatoryFieldValue) {
      let viewModelBuilder = new ViewModelBuilder()
        .addField('testfield1', 'EDITABLE', undefined, undefined, true, undefined, [{
          id: 'mandatory',
          level: 'error',
          message: 'The field is mandatory!'
        }])
        .addField('testfield5', 'EDITABLE');
      viewModelBuilder.addRegion('region1')
        .addField('testfield3', 'EDITABLE')
        .addField('testfield4', 'EDITABLE');
      let viewModel = viewModelBuilder.getModel();

      let validationModel = new ValidationModelBuilder()
        .addProperty('testfield1', mandatoryFieldValue)
        .addProperty('testfield3')
        .addProperty('testfield4')
        .addProperty('testfield5')
        .getModel();
      return new InstanceObject(id, {viewModel, validationModel}, null, null);
    }

    function createInstanceObjectWithAsyncFieldValidation(id) {
      let viewModelBuilder = new ViewModelBuilder()
        .addField('testfield1', 'EDITABLE', undefined, undefined, true, undefined, [{
          id: 'unique',
          level: 'error',
          message: 'The field is mandatory!'
        }])
        .addField('testfield5', 'EDITABLE');
      viewModelBuilder.addRegion('region1')
        .addField('testfield3', 'EDITABLE')
        .addField('testfield4', 'EDITABLE');
      let viewModel = viewModelBuilder.getModel();

      let validationModel = new ValidationModelBuilder()
        .addProperty('testfield1', 'some value')
        .addProperty('testfield3')
        .addProperty('testfield4')
        .addProperty('testfield5')
        .getModel();
      return new InstanceObject(id, {viewModel, validationModel}, null, null);
    }

    function validateAll(validationService, instanceObjects) {
      validationService.init();
      return validationService.validateAll(instanceObjects);
    }

    function validate(validationService, instanceObject) {
      validationService.init();
      return validationService.validate(instanceObject.getModels().validationModel, instanceObject.getModels().viewModel.flatModelMap, id, execution, formControl);
    }
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

  describe('updateValidationMessages', function () {
    let validatorDefinition = {
      'id': 'equal',
      'level': 'error',
      'message': 'Validator message'
    };

    it('should set validation message if a validator fails', function () {
      let isValid = false;
      //messages is an instance of ObservableArray
      let fieldValidationModel = new InstanceModelProperty({
        messages: []
      });

      ValidationService.setMessages(isValid, fieldValidationModel, validatorDefinition, translateService);

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

      ValidationService.setMessages(isValid, fieldValidationModel, validatorDefinition, translateService);

      expect(fieldValidationModel.messages.length).to.equal(0);
    });
  });

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

  describe(' isInaccessibleEmptyMandatoryField', () => {

    const FIELD_IDENTIFIER = 'fieldIdentifier';
    const MANDATORY = true;
    const NOT_MANDATORY = false;
    const PREVIEW = true;
    const NOT_PREVIEW = false;
    const RENDERED = true;
    const NOT_RENDERED = false;

    it('should be accessible if field is mandatory, has not value and display type is editable', () => {
      // Given:
      // we have mandatory field which is mandatory, has not value and display type is editable.
      let testData = {
        validationModel: new ValidationModelBuilder().addProperty(FIELD_IDENTIFIER).getModel(),
        propertyViewModel: {
          identifier: FIELD_IDENTIFIER,
          isMandatory: MANDATORY,
          displayType: ValidationService.DISPLAY_TYPE_EDITABLE,
          preview: NOT_PREVIEW,
          rendered: RENDERED
        }
      };

      // When:
      // check it for accessibility
      let isInaccessible = ValidationService.isInaccessibleEmptyMandatoryField(testData.propertyViewModel, testData.validationModel);

      // Then:
      // it should not be inaccessible.
      expect(isInaccessible).to.be.false;
    });

    it('should be accessible if field is mandatory, has value and display type is editable', () => {
      // Given:
      // we have mandatory field which is mandatory, has value and display type is editable.
      let testData = {
        validationModel: new ValidationModelBuilder().addProperty(FIELD_IDENTIFIER, 'fieldValue').getModel(),
        propertyViewModel: {
          identifier: FIELD_IDENTIFIER,
          isMandatory: MANDATORY,
          displayType: ValidationService.DISPLAY_TYPE_EDITABLE,
          preview: NOT_PREVIEW,
          rendered: RENDERED
        }
      };

      // When:
      // check it for accessibility
      let isInaccessible = ValidationService.isInaccessibleEmptyMandatoryField(testData.propertyViewModel, testData.validationModel);

      // Then:
      // it should not be inaccessible.
      expect(isInaccessible).to.be.false;
    });

    it('should be accessible if field is mandatory, has value and display type is read only', () => {
      // Given:
      // we have mandatory field which is mandatory, has value and display type is read only.
      let testData = {
        validationModel: new ValidationModelBuilder().addProperty(FIELD_IDENTIFIER, 'fieldValue').getModel(),
        propertyViewModel: {
          identifier: FIELD_IDENTIFIER,
          isMandatory: MANDATORY,
          displayType: ValidationService.DISPLAY_TYPE_READ_ONLY,
          preview: NOT_PREVIEW,
          rendered: RENDERED
        }
      };

      // When:
      // check it for accessibility
      let isInaccessible = ValidationService.isInaccessibleEmptyMandatoryField(testData.propertyViewModel, testData.validationModel);

      // Then:
      // it should not be inaccessible.
      expect(isInaccessible).to.be.false;
    });

    it('should be accessible if field is not mandatory, has not value and display type is read only', () => {
      // Given:
      // we have mandatory field which is not mandatory, has value and display type is read only.
      let testData = {
        validationModel: new ValidationModelBuilder().addProperty(FIELD_IDENTIFIER).getModel(),
        propertyViewModel: {
          identifier: FIELD_IDENTIFIER,
          isMandatory: NOT_MANDATORY,
          displayType: ValidationService.DISPLAY_TYPE_READ_ONLY,
          preview: NOT_PREVIEW,
          rendered: RENDERED
        }
      };

      // When:
      // check it for accessibility
      let isInaccessible = ValidationService.isInaccessibleEmptyMandatoryField(testData.propertyViewModel, testData.validationModel);

      // Then:
      // it should not be inaccessible.
      expect(isInaccessible).to.be.false;
    });

    it('should be inaccessible if field is mandatory, has not value and  and is in preview', () => {
      // Given:
      // we have mandatory field which is mandatory, has value and is in preview.
      let testData = {
        validationModel: new ValidationModelBuilder().addProperty(FIELD_IDENTIFIER).getModel(),
        propertyViewModel: {
          identifier: FIELD_IDENTIFIER,
          isMandatory: MANDATORY,
          displayType: ValidationService.DISPLAY_TYPE_READ_ONLY,
          preview: PREVIEW,
          rendered: RENDERED
        }
      };

      // When:
      // check it for accessibility
      let isInaccessible = ValidationService.isInaccessibleEmptyMandatoryField(testData.propertyViewModel, testData.validationModel);

      // Then:
      // it should be inaccessible.
      expect(isInaccessible).to.be.true;
    });

    it('should be inaccessible if field is mandatory, has not value and is not rendered', () => {
      // Given:
      // we have mandatory field which is mandatory, has value and is in preview.
      let testData = {
        validationModel: new ValidationModelBuilder().addProperty(FIELD_IDENTIFIER).getModel(),
        propertyViewModel: {
          identifier: FIELD_IDENTIFIER,
          isMandatory: MANDATORY,
          displayType: ValidationService.DISPLAY_TYPE_EDITABLE,
          preview: NOT_PREVIEW,
          rendered: NOT_RENDERED
        }
      };

      // When:
      // check it for accessibility
      let isInaccessible = ValidationService.isInaccessibleEmptyMandatoryField(testData.propertyViewModel, testData.validationModel);

      // Then:
      // it should be inaccessible.
      expect(isInaccessible).to.be.true;
    });

    it('should be inaccessible if field is mandatory, has not value and display type is hidden', () => {
      // Given:
      // we have mandatory field which is mandatory, has value and display type is hidden.
      let testData = {
        validationModel: new ValidationModelBuilder().addProperty(FIELD_IDENTIFIER).getModel(),
        propertyViewModel: {
          identifier: FIELD_IDENTIFIER,
          isMandatory: MANDATORY,
          displayType: ValidationService.DISPLAY_TYPE_HIDDEN,
          preview: NOT_PREVIEW,
          rendered: RENDERED
        }
      };

      // When:
      // check it for accessibility
      let isInaccessible = ValidationService.isInaccessibleEmptyMandatoryField(testData.propertyViewModel, testData.validationModel);

      // Then:
      // it should be inaccessible.
      expect(isInaccessible).to.be.true;
    });

    it('should be inaccessible if field is mandatory, has not value and display type is system', () => {
      // Given:
      // we have mandatory field which is mandatory, has value and display type is system.
      let testData = {
        validationModel: new ValidationModelBuilder().addProperty(FIELD_IDENTIFIER).getModel(),
        propertyViewModel: {
          identifier: FIELD_IDENTIFIER,
          isMandatory: MANDATORY,
          displayType: ValidationService.DISPLAY_TYPE_SYSTEM,
          preview: NOT_PREVIEW,
          rendered: RENDERED
        }
      };

      // When:
      // check it for accessibility
      let isInaccessible = ValidationService.isInaccessibleEmptyMandatoryField(testData.propertyViewModel, testData.validationModel);

      // Then:
      // it should be inaccessible.
      expect(isInaccessible).to.be.true;
    });

    it('should be inaccessible if field is mandatory, has not value and display type is read only', () => {
      // Given:
      // we have mandatory field which is mandatory, has value and display type is read only.
      let testData = {
        validationModel: new ValidationModelBuilder().addProperty(FIELD_IDENTIFIER).getModel(),
        propertyViewModel: {
          identifier: FIELD_IDENTIFIER,
          isMandatory: MANDATORY,
          displayType: ValidationService.DISPLAY_TYPE_READ_ONLY,
          preview: NOT_PREVIEW,
          rendered: RENDERED
        }
      };

      // When:
      // check it for accessibility
      let isInaccessible = ValidationService.isInaccessibleEmptyMandatoryField(testData.propertyViewModel, testData.validationModel);

      // Then:
      // it should be inaccessible.
      expect(isInaccessible).to.be.true;
    });
  });
});
