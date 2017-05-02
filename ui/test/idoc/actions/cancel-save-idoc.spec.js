import {IdocMocks} from '../idoc-mocks';
import {IdocContext, InstanceObject, CURRENT_OBJECT_TEMP_ID} from 'idoc/idoc-context';
import {CancelSaveIdocAction} from 'idoc/actions/cancel-save-idoc-action';
import {PromiseStub} from 'test/promise-stub';
import _ from 'lodash';

describe('Cancel idoc save action', () => {
  const IDOC_ID = 'emf:123456';
  var currentObject;
  beforeEach(() => {
    sinon.stub(IdocContext.prototype, 'getCurrentObject', () => {
      return new Promise((resolve) => {
        resolve(currentObject);
      });
    });
  });
  afterEach(() => {
    IdocContext.prototype.getCurrentObject.restore();
  });

  it('should revert changes when editing iDoc', () => {
    currentObject = new InstanceObject(IDOC_ID, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    let idocPage = IdocMocks.instantiateIdocPage(IDOC_ID);
    idocPage.currentObject = currentObject;
    idocPage.currentObject.models.validationModel.field1.value = 'modified value';
    idocPage.tabsConfig.tabs[0].content = 'modified content';

    let sharedObjects = {};
    sharedObjects[IDOC_ID] = currentObject;
    let idocContext = new IdocContext();
    idocContext.sharedObjects = sharedObjects;
    idocContext.sharedObjectsRegistry.registerWidget('widget:123456', IDOC_ID);
    let revertAllChangesSpy = sinon.spy(idocContext, 'revertAllChanges');
    let mockValidationService = {
      validate: ()=> {
      },
      init: sinon.spy()
    };
    let validatorSpy = sinon.spy(mockValidationService, 'validate');
    let handler = new CancelSaveIdocAction(IdocMocks.mockRouter(), IdocMocks.mockWindowAdapter(), IdocMocks.mockStateParamsAdapter(), IdocMocks.mockActionsService(), mockValidationService, {}, IdocMocks.mockIdocDraftService());

    _.extend(idocContext, {
      reloadObjectDetails: ()=> PromiseStub.resolve()
    });

    handler.execute({}, {
      currentObject: currentObject,
      idocPageController: idocPage,
      idocContext: idocContext
    });

    expect(validatorSpy.called).to.be.true;
    expect(idocPage.currentObject.models.validationModel.field1).to.have.property('value', 'value1');
    expect(idocPage.tabsConfig.tabs[0]).to.have.property('content', 'Content 0');
    expect(revertAllChangesSpy.callCount).to.equal(1);
    expect(mockValidationService.init.callCount).to.equal(1);
  });

  it('cancel() should redirect to return url when creating iDoc', () => {
    currentObject = new InstanceObject(CURRENT_OBJECT_TEMP_ID, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    let idocPage = IdocMocks.instantiateIdocPage();
    let handler = new CancelSaveIdocAction(IdocMocks.mockRouter(), IdocMocks.mockWindowAdapter(), IdocMocks.mockStateParamsAdapter(), IdocMocks.mockActionsService(), {});
    handler.execute({}, {
      currentObject: currentObject,
      idocPageController: idocPage,
      idocContext: {
        reloadObjectDetails: ()=> PromiseStub.resolve()
      }
    });
    expect(handler.windowAdapter.location).to.have.property('href', 'returnUrl');
  });

  it('should stop draft interval and delete draft if object is persisted', () => {
    let idocDraftService = {
      deleteDraft: () => PromiseStub.resolve()
    };
    let deleteDraftSpy = sinon.spy(idocDraftService, 'deleteDraft');

    let mockValidationService = {
      init: sinon.spy()
    };

    let handler = new CancelSaveIdocAction(IdocMocks.mockRouter(), IdocMocks.mockWindowAdapter(), IdocMocks.mockStateParamsAdapter(), IdocMocks.mockActionsService(), mockValidationService, {}, idocDraftService);
    currentObject = new InstanceObject(IDOC_ID, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    let idocPageController = {
      stopDraftInterval: sinon.stub(),
      appendContent: sinon.stub(),
      setViewMode: sinon.stub()
    };

    let idocContext = {
      revertAllChanges: sinon.stub(),
      getAllSharedObjects: () => [],
      reloadObjectDetails: () => PromiseStub.resolve()
    };
    handler.execute({}, {
      currentObject: currentObject,
      idocPageController: idocPageController,
      idocContext: idocContext
    });

    expect(idocPageController.stopDraftInterval.callCount).to.equal(1);
    expect(deleteDraftSpy.callCount).to.equal(1);
    expect(deleteDraftSpy.getCall(0).args[0]).to.equal(idocContext);
    expect(mockValidationService.init.callCount).to.equal(1);
  });
});