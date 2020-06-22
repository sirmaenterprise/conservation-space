import {MoveAction} from 'idoc/actions/move-action';
import {PickerService, SEARCH_EXTENSION, CREATE_EXTENSION, UPLOAD_EXTENSION, BASKET_EXTENSION} from 'services/picker/picker-service';
import {DialogService} from 'components/dialog/dialog-service';
import {STATE_PARAM_ID} from 'idoc/idoc-constants';
import {IDOC_PAGE_ACTIONS_PLACEHOLDER} from 'idoc/idoc-constants';
import {PromiseStub} from 'test/promise-stub';
import {IdocMocks} from '../idoc-mocks';
import {InstanceRestService} from 'services/rest/instance-service';
import {NotificationService} from 'services/notification/notification-service';
import {ActionsService} from 'services/rest/actions-service';
import {ExtensionsDialogService} from 'services/extensions-dialog/extensions-dialog-service';
import {RelatedObject} from 'models/related-object';
import {InstanceObject} from 'models/instance-object';
import {stub} from 'test/test-utils';

describe('MoveAction', () => {

  let moveAction;
  let context;
  let router;
  let extensionsDialogService;
  let instanceRestService;

  beforeEach(() => {
    let actionsService = stub(ActionsService);
    actionsService.move.returns(PromiseStub.resolve({
      data: {
        id: 'emf:newContext'
      }
    }));

    let pickerPromiseResult = {};
    pickerPromiseResult[SEARCH_EXTENSION] = {
      results: {
        config: {
          selectedItems: [{'id': 'newContext'}]
        }
      }
    };

    extensionsDialogService = stub(ExtensionsDialogService);
    extensionsDialogService.openDialog.returns(PromiseStub.resolve(pickerPromiseResult));

    let pickerService = new PickerService(extensionsDialogService);

    let dialogService = stub(DialogService);

    context = {
      currentObject: {
        getId: () => {
          return 'emf:123456';
        },
        setContextPath: sinon.spy(),
        getPropertyValue: () => {
          return new RelatedObject({results: ['parent-instance']});
        }
      },
      idocContext: {
        reloadObjectDetails: () => {
          return PromiseStub.resolve();
        }
      },
      placeholder: IDOC_PAGE_ACTIONS_PLACEHOLDER
    };

    router = IdocMocks.mockRouter();

    MoveAction.prototype.buildActionPayload = () => {
      return {};
    };

    instanceRestService = mockInstanceRestService();

    moveAction = new MoveAction(pickerService, dialogService, IdocMocks.mockTranslateService(), actionsService,
      IdocMocks.mockStateParamsAdapter(STATE_PARAM_ID, 'emf:123456'), router, IdocMocks.mockLogger(), mockNotificationService(),
      PromiseStub, instanceRestService, IdocMocks.mockTimeout());
  });

  describe('execute()', () => {

    it('should configure the picker to use the root context in the search extension', () => {
      moveAction.execute({}, context);

      expect(extensionsDialogService.openDialog.calledOnce).to.be.true;
      let dialogArgs = extensionsDialogService.openDialog.getCall(0).args;
      let searchExtensionConfig = dialogArgs[0].extensions[SEARCH_EXTENSION];
      expect(searchExtensionConfig.useRootContext).to.be.true;
      expect(searchExtensionConfig.arguments.filterByWritePermissions).to.be.true;
    });

    it('should configure the picker to not use the root context in the create extension', () => {
      moveAction.execute({}, context);

      expect(extensionsDialogService.openDialog.calledOnce).to.be.true;
      let dialogArgs = extensionsDialogService.openDialog.getCall(0).args;
      let createExtension = dialogArgs[0].extensions[CREATE_EXTENSION];
      expect(createExtension.useContext).to.be.false;
    });

    it('should configure the picker to not use the root context in the upload extension', () => {
      moveAction.execute({}, context);

      expect(extensionsDialogService.openDialog.calledOnce).to.be.true;
      let dialogArgs = extensionsDialogService.openDialog.getCall(0).args;
      let uploadExtension = dialogArgs[0].extensions[UPLOAD_EXTENSION];
      expect(uploadExtension.useContext).to.be.false;
    });

    it('should configure the picker to show custom basket label', () => {
      moveAction.execute({}, context);
      let pickerConfig = extensionsDialogService.openDialog.getCall(0).args[0];
      let basketExtension = pickerConfig.tabs[BASKET_EXTENSION];
      expect(basketExtension.label).to.equal('property.has.parent');
    });

    it('should provide correct idoc context to the picker service', () => {
      moveAction.execute({}, context);

      expect(extensionsDialogService.openDialog.calledOnce).to.be.true;
      let actualContext = extensionsDialogService.openDialog.getCall(0).args[1];
      expect(actualContext).to.deep.equal(context.idocContext);
    });

    it('should not invoke the service to execute the move action if 405 status error has occurred', () => {
      moveAction.actionsService.move.returns(PromiseStub.reject({
        status: 405,
        data: {
          message: 'error'
        }
      }));

      moveAction.execute({}, context);
      expect(extensionsDialogService.openDialog.calledOnce).to.be.true;

      let args = moveAction.dialogService.confirmation.getCall(0).args;
      let buttonClick = args[2].onButtonClick;
      let dialogConfig = {
        dismiss: sinon.spy()
      };
      buttonClick(DialogService.YES, {}, dialogConfig);
      expect(moveAction.notificationService.warning.callCount).to.equal(1);
    });

    it('should not invoke the service to execute the move action if generic status error has occurred', () => {
      moveAction.actionsService.move.returns(PromiseStub.reject({}));

      moveAction.execute({}, context);
      expect(extensionsDialogService.openDialog.calledOnce).to.be.true;

      let args = moveAction.dialogService.confirmation.getCall(0).args;
      let buttonClick = args[2].onButtonClick;
      let dialogConfig = {
        dismiss: sinon.spy()
      };
      buttonClick(DialogService.YES, {}, dialogConfig);
      expect(moveAction.notificationService.error.callCount).to.equal(1);
    });

    it('should invoke the service to execute the move action with proper arguments', () => {
      moveAction.execute({}, context);

      expect(extensionsDialogService.openDialog.calledOnce).to.be.true;

      let dialogArgs = extensionsDialogService.openDialog.getCall(0).args;
      expect(dialogArgs[0].header).to.exist;
      expect(dialogArgs[0].helpTarget).to.exist;
      expect(dialogArgs[0].extensionPoint).to.equal('picker');
      expect(dialogArgs[0].extensions[SEARCH_EXTENSION].results.config.selectedItems).to.deep.equal([{id: 'parent-instance'}]);

      let args = moveAction.dialogService.confirmation.getCall(0).args;
      expect(moveAction.dialogService.confirmation.callCount).to.equal(1);
      expect(args[2].buttons.length).to.equal(2);
      expect(args[2].buttons[0].id).to.equal(DialogService.YES);
      expect(args[2].buttons[1].id).to.equal(DialogService.NO);
      expect(typeof args[2].onButtonClick === 'function').to.be.true;

      let buttonClick = args[2].onButtonClick;
      let dialogConfig = {
        dismiss: sinon.spy()
      };

      buttonClick(DialogService.YES, {}, dialogConfig);
      expect(moveAction.actionsService.move.callCount).to.equal(1);
      expect(moveAction.instanceRestService.loadContextPath.callCount).to.equal(1);
      expect(dialogConfig.dismiss.callCount).to.equal(1);
      expect(moveAction.notificationService.success.callCount).to.equal(1);
    });

    it('should not invoke the service to execute the move action if confirmation dialog is cancelled', () => {
      moveAction.execute({}, context);

      let args = moveAction.dialogService.confirmation.getCall(0).args;
      expect(moveAction.dialogService.confirmation.callCount).to.equal(1);
      expect(args[2].buttons.length).to.equal(2);
      expect(args[2].buttons[0].id).to.equal(DialogService.YES);
      expect(args[2].buttons[1].id).to.equal(DialogService.NO);
      expect(args[2].onButtonClick !== undefined).to.be.true;

      let buttonClick = args[2].onButtonClick;
      let dialogConfig = {
        dismiss: sinon.spy()
      };
      buttonClick(DialogService.NO, {}, dialogConfig);
      expect(moveAction.actionsService.move.callCount).to.equal(0);
      expect(dialogConfig.dismiss.callCount).to.equal(1);
    });

    it('should invoke the service to execute the move action if no new context has been selected from the object picker', () => {
      extensionsDialogService.openDialog = () => {
        let pickerPromiseResult = {};
        pickerPromiseResult[SEARCH_EXTENSION] = {
          results: {
            config: {
              selectedItems: []
            }
          }
        };
        return PromiseStub.resolve(pickerPromiseResult);
      };
      moveAction.execute({}, context);
      expect(moveAction.dialogService.confirmation.callCount).to.equal(0);
    });

    it('should not redirect to the moved object landing page if the move operation is performed from anywhere but the moved object landing page (e.g. search page)', () => {
      context.idocContext = undefined;
      moveAction.execute({}, context);

      let args = moveAction.dialogService.confirmation.getCall(0).args;
      expect(moveAction.dialogService.confirmation.callCount).to.equal(1);
      expect(args[2].buttons.length).to.equal(2);
      expect(args[2].buttons[0].id).to.equal(DialogService.YES);
      expect(args[2].buttons[1].id).to.equal(DialogService.NO);
      expect(typeof args[2].onButtonClick === 'function').to.be.true;

      let buttonClick = args[2].onButtonClick;
      let dialogConfig = {
        dismiss: sinon.spy()
      };
      router = {
        navigate: sinon.spy()
      };
      buttonClick(DialogService.YES, {}, dialogConfig);
      expect(moveAction.actionsService.move.callCount).to.equal(1);
      expect(moveAction.instanceRestService.loadContextPath.callCount).to.equal(1);
      expect(dialogConfig.dismiss.callCount).to.equal(1);
      expect(router.navigate.callCount).to.equal(0);
    });

    it('should invoke the service to load instance\'s properties when placeholder is not idoc page', () => {
      context.placeholder = 'not-idoc-placeholder';
      moveAction.execute({}, context);
      expect(instanceRestService.loadInstanceObject.calledOnce).to.be.true;
    });

    it('should return that parent not changed when old parent and new parent are undefined', () => {
      expect(moveAction.isParentChanged(undefined, undefined)).to.be.false;
    });

    it('should return that parent not changed when ids of old parent and new parent are equals', () => {
      expect(moveAction.isParentChanged({id: 'emf:0001'}, {id: 'emf:0001'})).to.be.false;
    });

    it('should return that parent is changed when old parent is set and new parent is undefined', () => {
      expect(moveAction.isParentChanged({id: 'emf:0001'}, undefined)).to.be.true;
    });

    it('should return that parent is changed when old parent is undefined and new parent is set', () => {
      expect(moveAction.isParentChanged(undefined, {id: 'emf:0001'})).to.be.true;
    });

    it('should return that parent is changed when ids of old parent and new parent are not equals', () => {
      expect(moveAction.isParentChanged({id: 'emf:0002'}, {id: 'emf:0001'})).to.be.true;
    });
  });

  function mockNotificationService() {
    let notificationService = stub(NotificationService);
    notificationService.success.returnsArg(0);
    notificationService.warning.returnsArg(0);
    notificationService.error.returnsArg(0);
    return notificationService;
  }

  function mockInstanceRestService() {
    let instanceRestService = stub(InstanceRestService);
    instanceRestService.loadContextPath.returns(PromiseStub.resolve({data: ['parent', 'child']}));
    instanceRestService.load.returns(PromiseStub.resolve({data: {properties: {}}}));
    instanceRestService.loadModel.returns(PromiseStub.resolve({data: {}}));
    instanceRestService.loadInstanceObject.returns(PromiseStub.resolve(new InstanceObject({
      viewModel: {},
      validationModel: {properties: {}}
    })));
    return instanceRestService;
  }
});