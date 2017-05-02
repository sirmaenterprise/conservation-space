import {MoveAction} from 'idoc/actions/move-action';
import {PickerService, SEARCH_EXTENSION, CREATE_EXTENSION} from 'services/picker/picker-service';
import {DialogService} from "components/dialog/dialog-service";
import {STATE_PARAM_ID} from "idoc/idoc-page";

import {PromiseStub} from 'test/promise-stub';
import {IdocMocks} from '../idoc-mocks';

describe('MoveAction', () => {

  let moveAction;
  let context;
  let spyMove;
  let spyConfirmation;
  let router;
  let extensionsDialogService;

  beforeEach(() => {
    let actionsService = {
      move: () => {
        return PromiseStub.resolve({
          data: {
            id: 'emf:newContext'
          }
        })
      }
    };
    spyMove = sinon.spy(actionsService, 'move');
    let pickerPromiseResult = {};
    pickerPromiseResult[SEARCH_EXTENSION] = {
      results: {
        config: {
          selectedItems: [{'id': 'newContext'}]
        }
      }
    };
    extensionsDialogService = {
      openDialog: sinon.spy(() => {
        return PromiseStub.resolve(pickerPromiseResult)
      })
    };
    let pickerService = new PickerService(extensionsDialogService);
    spyConfirmation = sinon.spy();
    let dialogService = {
      confirmation: spyConfirmation
    };
    context = {
      currentObject: {
        getId: () => {
          return 'emf:123456'
        }
      },
      idocContext: {
        reloadObjectDetails: () => {
          return PromiseStub.resolve()
        }
      }
    };
    router = IdocMocks.mockRouter();
    MoveAction.prototype.buildActionPayload = () => {
      return {};
    };
    moveAction = new MoveAction(pickerService, dialogService, IdocMocks.mockTranslateService(), actionsService, IdocMocks.mockStateParamsAdapter(STATE_PARAM_ID, "emf:123456"), router, IdocMocks.mockLogger(), mockNotificationService());
  });

  describe('execute()', () => {

    it('should configure the picker to use the root context in the search extension', () => {
      moveAction.execute({}, context);

      expect(extensionsDialogService.openDialog.calledOnce).to.be.true;
      var dialogArgs = extensionsDialogService.openDialog.getCall(0).args;
      var extensions = dialogArgs[0].extensions[SEARCH_EXTENSION];
      expect(extensions.useRootContext).to.be.true;
    });

    it('should configure the picker to not use the root context in the create extension', () => {
      moveAction.execute({}, context);

      expect(extensionsDialogService.openDialog.calledOnce).to.be.true;
      var dialogArgs = extensionsDialogService.openDialog.getCall(0).args;
      var extensions = dialogArgs[0].extensions[CREATE_EXTENSION];
      expect(extensions.useRootContext).to.be.false;
    });

    it('should provide correct idoc context to the picker service', () => {
      moveAction.execute({}, context);

      expect(extensionsDialogService.openDialog.calledOnce).to.be.true;
      var actualContext = extensionsDialogService.openDialog.getCall(0).args[1];
      expect(actualContext).to.deep.equal(context.idocContext);
    });

    it('should not invoke the service to execute the move action if 405 status error has occurred', () => {
      moveAction.actionsService = {
        move: () => {
          return PromiseStub.reject({
            status: 405,
            data: {
              message: 'error'
            }
          })
        }
      };

      moveAction.execute({}, context);
      expect(extensionsDialogService.openDialog.calledOnce).to.be.true;

      var args = spyConfirmation.getCall(0).args;
      var buttonClick = args[2].onButtonClick;
      var dialogConfig = {
        dismiss: sinon.spy()
      };
      buttonClick(DialogService.YES, {}, dialogConfig);
      expect(moveAction.notificationService.error.callCount).to.equal(1);
    });

    it('should not invoke the service to execute the move action if generic status error has occurred', () => {
      moveAction.actionsService = {
        move: () => {
          return PromiseStub.reject({})
        }
      };

      moveAction.execute({}, context);
      expect(extensionsDialogService.openDialog.calledOnce).to.be.true;

      var args = spyConfirmation.getCall(0).args;
      var buttonClick = args[2].onButtonClick;
      var dialogConfig = {
        dismiss: sinon.spy()
      };
      buttonClick(DialogService.YES, {}, dialogConfig);
      expect(moveAction.notificationService.error.callCount).to.equal(1);
    });

    it('should invoke the service to execute the move action with proper arguments', () => {
      moveAction.execute({}, context);

      expect(extensionsDialogService.openDialog.calledOnce).to.be.true;

      var dialogArgs = extensionsDialogService.openDialog.getCall(0).args;
      expect(dialogArgs[0].header).to.exist;
      expect(dialogArgs[0].helpTarget).to.exist;
      expect(dialogArgs[0].extensionPoint).to.equal('picker');

      var args = spyConfirmation.getCall(0).args;
      expect(spyConfirmation.callCount).to.equal(1);
      expect(args[2].buttons.length).to.equal(2);
      expect(args[2].buttons[0].id).to.equal(DialogService.YES);
      expect(args[2].buttons[1].id).to.equal(DialogService.NO);
      expect(typeof args[2].onButtonClick === 'function').to.be.true;

      var buttonClick = args[2].onButtonClick;
      var dialogConfig = {
        dismiss: sinon.spy()
      };
      buttonClick(DialogService.YES, {}, dialogConfig);
      expect(spyMove.callCount).to.equal(1);
      expect(dialogConfig.dismiss.callCount).to.equal(1);
      expect(moveAction.notificationService.success.callCount).to.equal(1);
    });

    it('should not invoke the service to execute the move action if confirmation dialog is cancelled', () => {
      moveAction.execute({}, context);

      var args = spyConfirmation.getCall(0).args;
      expect(spyConfirmation.callCount).to.equal(1);
      expect(args[2].buttons.length).to.equal(2);
      expect(args[2].buttons[0].id).to.equal(DialogService.YES);
      expect(args[2].buttons[1].id).to.equal(DialogService.NO);
      expect(args[2].onButtonClick !== undefined).to.be.true;

      var buttonClick = args[2].onButtonClick;
      var dialogConfig = {
        dismiss: sinon.spy()
      };
      buttonClick(DialogService.NO, {}, dialogConfig);
      expect(spyMove.callCount).to.equal(0);
      expect(dialogConfig.dismiss.callCount).to.equal(1);
    });

    it('should not invoke the service to execute the move action if no new context has been selected from the object picker', () => {
      extensionsDialogService.openDialog = () => {
        var pickerPromiseResult = {};
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
      expect(spyConfirmation.callCount).to.equal(0);
    });

    it('should not redirect to the moved object landing page if the move operation is performed from anywhere but the moved object landing page (e.g. search page)', () => {
      context.idocContext = undefined;
      moveAction.execute({}, context);

      var args = spyConfirmation.getCall(0).args;
      expect(spyConfirmation.callCount).to.equal(1);
      expect(args[2].buttons.length).to.equal(2);
      expect(args[2].buttons[0].id).to.equal(DialogService.YES);
      expect(args[2].buttons[1].id).to.equal(DialogService.NO);
      expect(typeof args[2].onButtonClick === 'function').to.be.true;

      var buttonClick = args[2].onButtonClick;
      var dialogConfig = {
        dismiss: sinon.spy()
      };
      router = {
        navigate: sinon.spy()
      };
      buttonClick(DialogService.YES, {}, dialogConfig);
      expect(spyMove.callCount).to.equal(1);
      expect(dialogConfig.dismiss.callCount).to.equal(1);
      expect(router.navigate.callCount).to.equal(0);
    });
  });

  function mockNotificationService() {
    return {
      success: sinon.spy((value) => {
        return value;
      }),
      warning: sinon.spy((value) => {
        return value;
      }),
      error: sinon.spy((value) => {
        return value;
      })
    };
  }
});

