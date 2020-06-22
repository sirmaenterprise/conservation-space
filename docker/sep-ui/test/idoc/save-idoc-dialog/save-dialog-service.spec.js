import {SaveDialogService} from 'idoc/save-idoc-dialog/save-dialog-service';
import {stub} from 'test/test-utils';
import {DialogService} from 'components/dialog/dialog-service';
import {PromiseStub} from 'test/promise-stub';
import {InstanceContextService} from 'services/idoc/instance-context-service';
import {InstanceObject} from 'models/instance-object';
import {ViewModelBuilder} from 'test/form-builder/view-model-builder';
import {ValidationModelBuilder} from 'test/form-builder/validation-model-builder';
import {SaveIdocDialog} from 'idoc/save-idoc-dialog/save-idoc-dialog';
import {ModelUtils} from 'models/model-utils';
import {RelatedObject} from 'models/related-object';
import {CONTEXT_CHANGED_EVENT} from 'components/contextselector/context-selector';
import {PARENT} from 'instance/instance-properties';
import {SELECTION_MODE_IN_CONTEXT} from 'components/contextselector/context-selector';
import {ADD_CONTEXT_ERROR_MESSAGE_COMMAND} from 'components/contextselector/context-selector';
import {ERROR_EXISTING_WITHOUT_CONTEXT} from 'services/idoc/instance-context-service';

describe('SaveDialogService', () => {

  const PARENT_ID = 'emf:parentId';

  const INSTANCE_WITH_INVALID_FIELD_ID = 'emf:withInvalidField';
  const INSTANCE_WITH_INVALID_BOTH_ID = 'emf:withInvalidFieldAndContextualMismatched';

  let saveDialogService;
  let dialogService;
  let instanceContextService;
  let config;
  beforeEach(() => {
    dialogService = stub(DialogService);
    instanceContextService = stub(InstanceContextService);
    saveDialogService = new SaveDialogService(dialogService, PromiseStub, instanceContextService);
    config = initTest();
  });

  it('should create properly dialog service', () => {
    // When
    // open save idoc dialog
    saveDialogService.openDialog(config);

    // Then
    // it have to be opened with properly configuration.
    verifySaveDialogServiceIsOpened();

    let component = getComponent();
    let componentProperties = getComponentProperties();
    let dialogConfig = getDialogConfig();

    expect(component).to.equals(SaveIdocDialog);
    expect(componentProperties.config.invalidObjects).to.equals(config.models);
    expect(dialogConfig.header).to.equals('idoc.savedialog.header');
    expect(dialogConfig.backdrop).to.equals('static');
    expect(dialogConfig.modalCls).to.equals('save-idoc-dialog');
    expect(dialogConfig.showClose).to.be.true;
    expect(dialogConfig.largeModal).to.be.true;
    expect(dialogConfig.buttons.length === 2).to.be.true;
  });

  it('should publish error message when there is mismatch between instance context existence and configuration', (done) => {
    // When
    // open save idoc dialog
    saveDialogService.openDialog(config);
    verifySaveDialogServiceIsOpened();
    let eventEmitter = getContextEventEmitter(INSTANCE_WITH_INVALID_BOTH_ID);

    eventEmitter.subscribe(ADD_CONTEXT_ERROR_MESSAGE_COMMAND, (errorMessage) => {
      // Then:
      // we expect error message.
      expect(errorMessage).to.equal(ERROR_EXISTING_WITHOUT_CONTEXT);
      done();
    });

    //When change parent with incorrect one.
    updateContextOfInstanceObject(INSTANCE_WITH_INVALID_BOTH_ID);

  });

  describe('describe when save button is enabled', () => {

    it('save button have to be disabled when it has been enabled and validation error is occurred', () => {
      // When
      // open save idoc dialog
      saveDialogService.openDialog(config);
      verifySaveDialogServiceIsOpened();
      // 'emf:withInvalidFieldAndContextualMismatched' 'emf:withInvalidField' and errors are fixed
      updateValidationOfInstanceObject(INSTANCE_WITH_INVALID_BOTH_ID, true);
      updateContextOfInstanceObject(INSTANCE_WITH_INVALID_BOTH_ID, PARENT_ID);
      updateValidationOfInstanceObject(INSTANCE_WITH_INVALID_FIELD_ID, true);
      // save button have to be enabled
      verifySaveButtonEnabled();

      // When
      // validation error occurred
      updateValidationOfInstanceObject(INSTANCE_WITH_INVALID_FIELD_ID, false);

      // Then:
      // save button have to be enabled
      verifySaveButtonDisabled();
    });

    it('Scenario when last fixed validation error is field validation one', () => {
      // When
      // open save idoc dialog
      saveDialogService.openDialog(config);
      verifySaveDialogServiceIsOpened();

      // Then:
      // save button have to be disabled
      verifySaveButtonDisabled();

      // When:
      // 'emf:withInvalidFieldAndContextualMismatched' errors are fixed
      updateValidationOfInstanceObject(INSTANCE_WITH_INVALID_BOTH_ID, true);
      updateContextOfInstanceObject(INSTANCE_WITH_INVALID_BOTH_ID, PARENT_ID);

      // Then:
      // save button have to be still disabled
      // the 'emf:withInvalidField' is still invalid
      verifySaveButtonDisabled();

      // When:
      // 'emf:withInvalidField' set to valid
      updateValidationOfInstanceObject(INSTANCE_WITH_INVALID_FIELD_ID, true);

      // Then:
      // save button have to be enabled
      verifySaveButtonEnabled();
    });

    it('Scenario when last fixed validation error is contextual one', () => {
      // When
      // open save idoc dialog
      saveDialogService.openDialog(config);
      verifySaveDialogServiceIsOpened();

      // Then:
      // save button have to be disabled
      verifySaveButtonDisabled();

      // When:
      // 'emf:withInvalidField' set to valid
      updateValidationOfInstanceObject(INSTANCE_WITH_INVALID_FIELD_ID, true);

      // Then:
      // save button have to be still disabled
      // the 'emf:withInvalidFieldAndContextualMismatched' is still invalid
      verifySaveButtonDisabled();

      // When:
      // 'emf:withInvalidFieldAndContextualMismatched' errors are fixed
      updateValidationOfInstanceObject(INSTANCE_WITH_INVALID_BOTH_ID, true);
      updateContextOfInstanceObject(INSTANCE_WITH_INVALID_BOTH_ID, PARENT_ID);

      // Then:
      // save button have to be enabled
      verifySaveButtonEnabled();
    });
  });

  // common function for verifications.
  function verifySaveDialogServiceIsOpened() {
    expect(dialogService.create.calledOnce).to.be.true;
  }

  function verifySaveButtonEnabled() {
    expect(isSaveButtonDisabled()).to.be.false;
  }

  function verifySaveButtonDisabled() {
    expect(isSaveButtonDisabled()).to.be.true;
  }

  // common function to update validation statuses
  function updateValidationOfInstanceObject(id, isValid) {
    let onFormValidatedFunction = getOnFormValidateFunction();
    let data = [{id, isValid}];
    onFormValidatedFunction(data);
  }

  function updateContextOfInstanceObject(instanceObjectId, newParentId) {
    getContextEventEmitter(instanceObjectId).publish(CONTEXT_CHANGED_EVENT, newParentId);
  }

  // common function to fetch elements
  function getContextEventEmitter(instanceObjectId) {
    return config.models[instanceObjectId].contextSelectorConfig.eventEmitter;
  }

  function getComponent() {
    return dialogService.create.getCall(0).args[0];
  }

  function getComponentProperties() {
    return dialogService.create.getCall(0).args[1];
  }

  function getDialogConfig() {
    return dialogService.create.getCall(0).args[2];
  }

  function getOnFormValidateFunction() {
    return getComponentProperties().config.onFormValidated;
  }


  // common functions
  /**
   * Creates configuration to be used when dialog is opened.
   * Configuration contains two models of tree invalid instance objects.
   * First object is with invalid field validation.
   * Second is with invalid field validation and contextual existence.
   *
   * @return {{models: {}}}
   */
  function initTest() {
    let existingInContextResultConfiguration = [];
    let config = {models: {}};
    let withInvalidField = createInstanceObject(INSTANCE_WITH_INVALID_FIELD_ID);
    let withInvalidFieldModel = convertToSaveDialogModel(withInvalidField);
    existingInContextResultConfiguration.push({instance: withInvalidField, isValid: true});
    config.models[withInvalidField.getId()] = withInvalidFieldModel;

    let withInvalidFieldAndContextualMismatched = createInstanceObject(INSTANCE_WITH_INVALID_BOTH_ID);
    let withInvalidFieldAndContextualMismatchedModel = convertToSaveDialogModel(withInvalidFieldAndContextualMismatched);
    existingInContextResultConfiguration.push({instance: withInvalidFieldAndContextualMismatched, isValid: false});
    config.models[withInvalidFieldAndContextualMismatched.getId()] = withInvalidFieldAndContextualMismatchedModel;

    setupInstanceContextService(existingInContextResultConfiguration);
    return config;
  }

  function createInstanceObject(id) {
    let viewModelBuilder = new ViewModelBuilder()
      .addField(PARENT, 'EDITABLE')
      .addField('testField3', 'EDITABLE');
    let viewModel = viewModelBuilder.getModel();
    let validationModelBuilder = new ValidationModelBuilder()
      .addProperty(PARENT, new RelatedObject(ModelUtils.getEmptyObjectPropertyValue()))
      .addProperty('testField3');
    let validationModel = validationModelBuilder.getModel();
    return new InstanceObject(id, {viewModel, validationModel, definitionId: 'someDefinitionId'}, null, null);
  }

  function setupInstanceContextService(validationConfiguration = []) {
    let validationResults = {};
    validationConfiguration.forEach((configuration) => {
      let id = configuration.instance.getId();
      validationResults[id] = {id, isValid: configuration.isValid, existingInContext: SELECTION_MODE_IN_CONTEXT};
    });

    instanceContextService.validateExistingInContextAll.returns(PromiseStub.resolve(validationResults));
  }

  function convertToSaveDialogModel(instanceObject) {
    return {
      models: {
        id: instanceObject.getId(),
        definitionId: instanceObject.getModels().definitionId,
        validationModel: instanceObject.getModels().validationModel,
        viewModel: instanceObject.getModels().viewModel
      }
    };
  }

  function isSaveButtonDisabled() {
    let okButton = getDialogConfig().buttons[0];
    return okButton.disabled;
  }
});