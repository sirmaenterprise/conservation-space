import {IdocMocks} from '../idoc-mocks';
import {IdocContext} from 'idoc/idoc-context';
import {InstanceObject, CURRENT_OBJECT_TEMP_ID} from 'models/instance-object';
import {CancelSaveIdocAction} from 'idoc/actions/cancel-save-idoc-action';
import {PromiseStub} from 'test/promise-stub';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';
import {DialogService} from 'components/dialog/dialog-service';
import {TranslateService} from 'services/i18n/translate-service';
import {stub} from 'test/test-utils';
import _ from 'lodash';

describe('Cancel idoc save action', () => {
  const IDOC_ID = 'emf:123456';
  let currentObject;
  let handler;
  let dialogServiceStub;
  beforeEach(() => {
    sinon.stub(IdocContext.prototype, 'getCurrentObject', () => {
      return new Promise((resolve) => {
        resolve(currentObject);
      });
    });

    let mockValidationService = {
      validate: () => {
        return PromiseStub.resolve();
      },
      init: sinon.spy()
    };

    let idocDraftService = {
      deleteDraft: () => PromiseStub.resolve()
    };

    let mockNotificationService = {
      error: sinon.spy(() => {
      })
    };

    dialogServiceStub = {
      confirmation: sinon.spy()
    };

    handler = new CancelSaveIdocAction(IdocMocks.mockRouter(), IdocMocks.mockWindowAdapter(), IdocMocks.mockStateParamsAdapter(), IdocMocks.mockActionsService(), mockValidationService, {}, idocDraftService, mockNotificationService, PromiseAdapterMock.mockImmediateAdapter(), stub(TranslateService), dialogServiceStub);
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

    currentObject.setDirty(true);

    let idocContext = new IdocContext();
    idocContext.sharedObjects = sharedObjects;
    idocContext.sharedObjectsRegistry.registerWidget('widget:123456', IDOC_ID);
    let revertAllChangesSpy = sinon.spy(idocContext, 'revertAllChanges');

    let validatorSpy = sinon.spy(handler.validationService, 'validate');

    _.extend(idocContext, {
      reloadObjectDetails: () => PromiseStub.resolve()
    });

    handler.execute({}, {
      currentObject: currentObject,
      idocPageController: idocPage,
      idocContext: idocContext
    });

    let dialogConfig = handler.dialogService.confirmation.getCall(0).args[2];
    let dismissSpy = sinon.spy();
    dialogConfig.onButtonClick(DialogService.CONFIRM, undefined, {dismiss: dismissSpy});

    expect(validatorSpy.called).to.be.true;
    expect(idocPage.currentObject.models.validationModel.field1).to.have.property('value', 'value1');
    expect(idocPage.tabsConfig.tabs[0]).to.have.property('content', 'Content 0');
    expect(revertAllChangesSpy.callCount).to.equal(1);
    expect(handler.validationService.init.callCount).to.equal(1);
  });

  it('cancel() should redirect to return url when creating iDoc', () => {
    currentObject = new InstanceObject(CURRENT_OBJECT_TEMP_ID, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    let idocPage = IdocMocks.instantiateIdocPage();
    idocPage.currentObject = currentObject;

    handler.execute({}, {
      currentObject: currentObject,
      idocPageController: idocPage,
      idocContext: {
        reloadObjectDetails: () => PromiseStub.resolve()
      }
    });
    let dialogConfig = handler.dialogService.confirmation.getCall(0).args[2];
    let dismissSpy = sinon.spy();
    dialogConfig.onButtonClick(DialogService.CONFIRM, undefined, {dismiss: dismissSpy});
    expect(handler.windowAdapter.location).to.have.property('href', 'returnUrl');
  });

  it('should stop draft interval and delete draft if object is persisted', () => {
    let deleteDraftSpy = sinon.spy(handler.idocDraftService, 'deleteDraft');

    currentObject = new InstanceObject(IDOC_ID, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    let idocPageController = {
      stopDraftInterval: sinon.stub(),
      appendContent: sinon.stub(),
      setViewMode: sinon.stub(),
      currentObject
    };

    let idocContext = {
      revertAllChanges: sinon.stub(),
      getAllSharedObjects: () => [],
      reloadObjectDetails: () => PromiseStub.resolve(),
      getCurrentObjectId: () => {
        return currentObject.getId();
      }
    };

    handler.execute({}, {
      currentObject: currentObject,
      idocPageController: idocPageController,
      idocContext: idocContext
    });

    let dialogConfig = handler.dialogService.confirmation.getCall(0).args[2];
    let dismissSpy = sinon.spy();
    dialogConfig.onButtonClick(DialogService.CONFIRM, undefined, {dismiss: dismissSpy});

    expect(idocPageController.stopDraftInterval.callCount).to.equal(1);
    expect(deleteDraftSpy.callCount).to.equal(1);
    expect(deleteDraftSpy.getCall(0).args[0]).to.equal(idocContext);
    expect(handler.validationService.init.callCount).to.equal(1);
  });

  it('should catch error', () => {
    let deleteDraftSpy = sinon.spy(handler.idocDraftService, 'deleteDraft');

    handler.actionsService.unlock = () => PromiseStub.reject();
    currentObject = new InstanceObject(IDOC_ID, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    let idocPageController = {
      stopDraftInterval: sinon.stub(),
      appendContent: sinon.stub(),
      setViewMode: sinon.stub(),
      currentObject
    };

    let idocContext = {
      revertAllChanges: sinon.stub(),
      getAllSharedObjects: () => [],
      reloadObjectDetails: () => PromiseStub.resolve(),
      getCurrentObjectId: () => {
        return currentObject.getId();
      }
    };

    handler.execute({}, {
      currentObject: currentObject,
      idocPageController: idocPageController,
      idocContext: idocContext
    });

    let dialogConfig = handler.dialogService.confirmation.getCall(0).args[2];
    let dismissSpy = sinon.spy();
    dialogConfig.onButtonClick(DialogService.CONFIRM, undefined, {dismiss: dismissSpy});

    expect(idocPageController.stopDraftInterval.callCount).to.equal(1);
    expect(deleteDraftSpy.callCount).to.equal(1);
    expect(deleteDraftSpy.getCall(0).args[0]).to.equal(idocContext);
    expect(handler.validationService.init.callCount).to.equal(1);
    expect(handler.notificationService.error.calledOnce).to.be.true;
  });

  describe('Cancel idoc save action with dirty editor', () => {

    it('should display message if dirty editor', () => {
      currentObject = new InstanceObject(CURRENT_OBJECT_TEMP_ID, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
      currentObject.setDirty(true);
      let idocPage = IdocMocks.instantiateIdocPage();
      idocPage.currentObject = currentObject;

      handler.execute({}, {
        currentObject: currentObject,
        idocPageController: idocPage,
        idocContext: {
          reloadObjectDetails: () => PromiseStub.resolve()
        }
      });
      expect(dialogServiceStub.confirmation.callCount).to.equal(1);
    });
  });
});