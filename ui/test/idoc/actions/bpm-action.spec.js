import {BpmTransitionAction} from 'idoc/actions/bpm-action';
import {StatusCodes} from 'services/rest/status-codes';
import {PromiseStub} from 'test/promise-stub';
import {BpmMockUtil} from 'test/idoc/actions/bpm-mock-util';

describe('BpmTransitionAction ', () => {

  let bpmTransitionHandler;
  let notificationService;
  let translateService;
  let logger;
  let saveDialogService;
  let bpmService;
  let validationService;
  let context;
  let actionDefinition;
  let actionService;
  let instanceRestService;
  let eventbus;
  let promiseAdapter;

  beforeEach(() => {
    notificationService = BpmMockUtil.mockNotificationService();
    translateService = BpmMockUtil.mockTranslateService();
    logger = BpmMockUtil.mockLoggerService();
    saveDialogService = BpmMockUtil.mockSaveDialogService();
    bpmService = BpmMockUtil.mockBpmService();
    validationService = BpmMockUtil.mockValidationService();
    actionService = BpmMockUtil.mockActionsService();
    instanceRestService = BpmMockUtil.mockInstanceRestService();
    eventbus = BpmMockUtil.mockEventBus();
    promiseAdapter = BpmMockUtil.mockPromiseAdapter();

    bpmTransitionHandler = new BpmTransitionAction(actionService, validationService, saveDialogService, instanceRestService, eventbus, logger, promiseAdapter, bpmService, translateService, notificationService);
    bpmTransitionHandler.generateInstance = (prop, instance) => {
      return mockInstance(prop, instance);
    };
    context = {
      currentObject: {
        getId: () => {
          return 'emf:id'
        },
        models: {
          definitionId: 'TASK100'
        }
      }
    };
    actionDefinition = {
      action: 'bpmTransition'
    };
  });

  it('notifies on success ', () => {
    let models = {};
    bpmTransitionHandler.executeTransition(context, actionDefinition, models);
    expect(notificationService.success.calledOnce);
    expect(notificationService.success.getCall(0).args[0]).to.equal('Test String created BR header<br>');
    expect(eventbus.publish.calledOnce);
  });

  it('notifies on transition failure', () => {
    translateService = mockErrorTranslateService();
    bpmService = mockErrorBpmService();
    let bpmErrorTransitionHandler = new BpmTransitionAction(actionService, validationService, saveDialogService, instanceRestService, eventbus, logger, promiseAdapter, bpmService, translateService, notificationService);
    let models = {};
    bpmErrorTransitionHandler.executeTransition(context, actionDefinition, models);
    expect(translateService.translateInstant.getCall(0).args[0]).to.equal('testLabelId');
    expect(notificationService.warning.calledOnce);
    expect(notificationService.warning.getCall(0).args[0]).to.equal('testLabelId');
  });

  it('notifies on transition failure without proper response', () => {
    translateService = mockErrorTranslateService();
    bpmService = mockErrorBpmServiceIncorrectResponse();
    let bpmErrorTransitionHandler = new BpmTransitionAction(actionService, validationService, saveDialogService, instanceRestService, eventbus, logger, promiseAdapter, bpmService, translateService, notificationService);
    let models = {};
    bpmErrorTransitionHandler.executeTransition(context, actionDefinition, models);
    expect(translateService.translateInstant.getCall(0).args[0]).to.equal('error.generic');
    expect(notificationService.error.calledOnce);
    expect(notificationService.error.getCall(0).args[0]).to.equal('error.generic');
  });

  it('notifies on failure ', () => {
    let models = {};
    bpmTransitionHandler.executeTransition(context, actionDefinition, models);
    expect(notificationService.success.calledOnce);
    expect(notificationService.success.getCall(0).args[0]).to.equal('Test String created BR header<br>');
  });

  it('Execute ', () => {
    bpmTransitionHandler.execute(actionDefinition, context);
    expect(saveDialogService.openDialog.calledOnce);
    expect(notificationService.success.calledOnce);
    expect(notificationService.success.getCall(0).args[0]).to.equal('Test String created BR header<br>');
  });

  it('Get object models ', () => {
    bpmTransitionHandler.getObjectModel(context, actionDefinition).then((resultingModel) => {
      expect(resultingModel.item1.models.id).to.equal('item1');
    });
  });

  it('Error on transition ', () => {
    bpmTransitionHandler.validationService.init = () => {
      return PromiseStub.reject("error");
    }
    bpmTransitionHandler.execute(actionDefinition, context);
    expect(logger.error.calledOnce).to.be.true;
  });

  it('Form Validation false', () => {
    let button = {
      disabled: true
    };
    let data = [{
      isValid: true,
      id: 'emf:id'
    }];
    bpmTransitionHandler.invalidObjects = {
      'emf:id': {
        isValid: true
      }
    };
    bpmTransitionHandler.onFormValidated(button, data);
    expect(button.disabled).to.be.false;
  });

  it('Form Validation true', () => {
    let button = {
      disabled: true
    };
    let data = [{
      isValid: false,
      id: 'emf:id'
    }];
    bpmTransitionHandler.invalidObjects = {
      'emf:id': {
        isValid: true
      }
    };
    bpmTransitionHandler.onFormValidated(button, data);
    expect(button.disabled).to.be.true;
  });

  function mockErrorTranslateService() {
    return {
      translateInstant: sinon.spy((value) => {
        return value;
      })
    };
  }

  function mockErrorBpmServiceIncorrectResponse() {
    return {
      buildBPMActionPayload: sinon.spy((id, actionDefinition, models, op) => {
        let payload = {};
        return payload;
      }),
      executeTransition: sinon.spy((currentObjectId, payload) => {
        let response = {
          status: StatusCodes.SERVER_ERROR,
        };
        return PromiseStub.reject(response);
      })
    }
  }

  function mockErrorBpmService() {
    return {
      buildBPMActionPayload: sinon.spy((id, actionDefinition, models, op) => {
        let payload = {};
        return payload;
      }),
      executeTransition: sinon.spy((currentObjectId, payload) => {
        let response = {
          status: StatusCodes.SERVER_ERROR,
          data: {
            labelId: 'testLabelId'
          }
        };
        return PromiseStub.reject(response);
      })
    }
  }

  function mockInstance(prop, instance) {
    return {
      models: {
        id: null
      },
      mergePropertiesIntoModel: (properties) => {

      },
      hasMandatory: () => {
        return true;
      }
    }
  }

});