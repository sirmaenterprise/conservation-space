import {InstanceContextService} from 'services/idoc/instance-context-service';
import {stub} from 'test/test-utils';
import {ModelsService} from 'services/rest/models-service';
import {PromiseStub} from 'test/promise-stub';
import {ViewModelBuilder} from 'test/form-builder/view-model-builder';
import {ModelUtils} from 'models/model-utils';
import {InstanceObject} from 'models/instance-object';
import {ValidationModelBuilder} from 'test/form-builder/validation-model-builder';
import {RelatedObject} from 'models/related-object';
import {PARENT, INTEGRATED} from 'instance/instance-properties';
import {
  SELECTION_MODE_BOTH,
  SELECTION_MODE_IN_CONTEXT,
  SELECTION_MODE_WITHOUT_CONTEXT
} from 'components/contextselector/context-selector';
import {ERROR_EXISTING_WITHOUT_CONTEXT, ERROR_EXISTING_IN_CONTEXT} from 'services/idoc/instance-context-service';

describe('InstanceContextService', () => {

  const PARENT_ID = 'emf:parentId';
  const INSTANCE_WITH_PARENT_ID = 'emf:withoutParent';
  const INSTANCE_WITHOUT_PARENT = 'emf:withoutParent';

  const DEFINITION_IN_CONTEXT = 'definitionInContext';
  const DEFINITION_WITHOUT_CONTEXT = 'definitionWithoutContext';
  const DEFINITION_BOTH = 'definitionBoth';

  let instanceContextService;
  beforeEach(() => {
    instanceContextService = new InstanceContextService(createModelService(), PromiseStub);
  });

  describe('validateExistingInContext', () => {

    it('should not have error message no mater has parent when configuration is BOTH', () => {
      let expectedResult = {
        id: INSTANCE_WITHOUT_PARENT,
        isValid: true,
        existingInContext: SELECTION_MODE_BOTH
      };
      executeTestBoth(true, expectedResult);
      executeTestBoth(false, expectedResult);
    });

    it('should have not error message when instance object has not parent and configuration is SELECTION_MODE_WITHOUT_CONTEXT', () => {
      let hasParent = false;
      let expectedResult = {
        id: INSTANCE_WITHOUT_PARENT,
        isValid: true,
        existingInContext: SELECTION_MODE_WITHOUT_CONTEXT

      };
      executeTestWithoutContext(hasParent, expectedResult);

    });

    it('should have error message when instance object has parent and configuration is SELECTION_MODE_WITHOUT_CONTEXT', () => {
      let hasParent = true;
      let expectedResult = {
        id: INSTANCE_WITH_PARENT_ID,
        isValid: false,
        existingInContext: SELECTION_MODE_WITHOUT_CONTEXT,
        errorMessage: ERROR_EXISTING_IN_CONTEXT
      };
      executeTestWithoutContext(hasParent, expectedResult);

    });

    it('should have error message when instance object has not parent and configuration is IN_CONTEXT', () => {
      let hasParent = false;
      let expectedResult = {
        id: INSTANCE_WITHOUT_PARENT,
        isValid: false,
        existingInContext: SELECTION_MODE_IN_CONTEXT,
        errorMessage: ERROR_EXISTING_WITHOUT_CONTEXT
      };
      executeTestInContext(hasParent, expectedResult);

    });

    it('should not have error message when instance object has parent and configuration is IN_CONTEXT', () => {
      let hasParent = true;
      let expectedResult = {
        id: INSTANCE_WITH_PARENT_ID,
        isValid: true,
        existingInContext: SELECTION_MODE_IN_CONTEXT
      };
      executeTestInContext(hasParent, expectedResult);

    });
  });

  describe('getParent', () => {
    it('should return parent of instance object', () => {
      // Given:
      // we have a instance object.
      let instanceObject = createInstanceObjectWithParent();

      // When:
      // ask for parent.
      let parent = InstanceContextService.getParent(instanceObject);

      // Then:
      // expect returned parent to be parent of object.
      expect(parent).to.equal(PARENT_ID);
    });

    it('should return undefined if instance object have not parent', () => {
      // Given:
      // we have a instance object.
      let instanceObject = createInstanceObjectWithoutParent();

      // When:
      // ask for parent.
      let parent = InstanceContextService.getParent(instanceObject);

      // Then:
      // expect returned parent to be undefined.
      expect(parent === undefined).to.be.true;
    });
  });

  describe('updateParent', () => {
    it('should update parent of instance object', () => {
      // Given:
      // we have a instance object.
      let newParentId = 'emf:newParentId';
      let instanceObject = createInstanceObjectWithoutParent();

      // When:
      // update instance parent
      InstanceContextService.updateParent(instanceObject, newParentId);

      // Then:
      // new parent have to be added.
      let parent = InstanceContextService.getParent(instanceObject);
      expect(parent === undefined).to.be.true;

      let hasParentValue = instanceObject.getPropertyValue(PARENT);
      expect(hasParentValue.add[0]).to.equal(newParentId);

    });
  });

  function createInstanceObjectWithParent(definitionId, isRevision, isIntegrated) {
    let viewModelBuilder = new ViewModelBuilder()
      .addField(PARENT, 'EDITABLE');
    if (isIntegrated) {
      viewModelBuilder.addField(INTEGRATED, 'SYSTEM');
    }
    let viewModel = viewModelBuilder.getModel();

    let hasParentValue = ModelUtils.getEmptyObjectPropertyValue();
    hasParentValue.results = [PARENT_ID];
    hasParentValue.total = 1;

    let validationModelBuilder = new ValidationModelBuilder()
      .addProperty(PARENT, new RelatedObject(hasParentValue));

    if (isIntegrated) {
      validationModelBuilder.addProperty(INTEGRATED, true);
    }

    let validationModel = validationModelBuilder.getModel();
    return new InstanceObject(INSTANCE_WITH_PARENT_ID, {viewModel, validationModel, definitionId}, null, null);
  }

  function createInstanceObjectWithoutParent(definitionId, isRevision, isIntegrated) {
    let viewModelBuilder = new ViewModelBuilder().addField(PARENT, 'EDITABLE');
    if (isIntegrated) {
      viewModelBuilder.addField(INTEGRATED, 'SYSTEM');
    }
    let viewModel = viewModelBuilder.getModel();

    let validationModelBuilder = new ValidationModelBuilder().addProperty(PARENT, new RelatedObject(ModelUtils.getEmptyObjectPropertyValue()));
    if (isIntegrated) {
      validationModelBuilder.addProperty(INTEGRATED, true);
    }

    let validationModel = validationModelBuilder.getModel();
    return new InstanceObject(INSTANCE_WITHOUT_PARENT, {viewModel, validationModel, definitionId}, null, null);
  }

  function createModelService() {
    let modelsService = stub(ModelsService);
    modelsService.getExistingInContextInfo.withArgs(sinon.match(DEFINITION_IN_CONTEXT)).returns(PromiseStub.resolve(SELECTION_MODE_IN_CONTEXT));
    modelsService.getExistingInContextInfo.withArgs(sinon.match(DEFINITION_WITHOUT_CONTEXT)).returns(PromiseStub.resolve(SELECTION_MODE_WITHOUT_CONTEXT));
    modelsService.getExistingInContextInfo.withArgs(sinon.match(DEFINITION_BOTH)).returns(PromiseStub.resolve(SELECTION_MODE_BOTH));
    return modelsService;
  }

  function executeTestInContext(withParent, expectedResult, isRevision, isIntegrated) {
    let instanceObject = createInstanceObject(DEFINITION_IN_CONTEXT, withParent, isRevision, isIntegrated);
    executeTest(instanceObject, expectedResult);
  }

  function executeTestWithoutContext(withParent, expectedResult, isRevision, isIntegrated) {
    let instanceObject = createInstanceObject(DEFINITION_WITHOUT_CONTEXT, withParent, isRevision, isIntegrated);
    executeTest(instanceObject, expectedResult);
  }

  function executeTestBoth(withParent, expectedResult, isRevision, isIntegrated) {
    let instanceObject = createInstanceObject(DEFINITION_BOTH, withParent, isRevision, isIntegrated);
    executeTest(instanceObject, expectedResult);
  }

  function createInstanceObject(definitionId, withParent, isRevision, isIntegrated) {
    if (withParent) {
      return createInstanceObjectWithParent(definitionId, isRevision, isIntegrated);
    }
    return createInstanceObjectWithoutParent(definitionId, isRevision, isIntegrated);
  }

  function executeTest(instanceObject, expectedResult) {
    instanceContextService.validateExistingInContext(instanceObject).then((validationResult) => {
      expect(validationResult.id).to.equal(expectedResult.id);
      expect(validationResult.isValid).to.equal(expectedResult.isValid);
      expect(validationResult.existingInContext).to.equal(expectedResult.existingInContext);
      expect(validationResult.errorMessage).to.equal(expectedResult.errorMessage);

    });
  }
});