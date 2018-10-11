import {BpmTransitionAction} from 'idoc/actions/bpm-action';
import {StatusCodes} from 'services/rest/status-codes';
import {PromiseStub} from 'test/promise-stub';
import {BpmMockUtil} from 'test/idoc/actions/bpm-mock-util';
import {SaveDialogService} from 'idoc/save-idoc-dialog/save-dialog-service';
import {NotificationService} from 'services/notification/notification-service';
import {ValidationService} from 'form-builder/validation/validation-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {TranslateService} from 'services/i18n/translate-service';
import {ActionsService} from 'services/rest/actions-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {Logger} from 'services/logging/logger';
import {stub} from 'test/test-utils';

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
    notificationService = stub(NotificationService);

    translateService = stub(TranslateService);
    translateService.translateInstant.returns(' created');
    translateService.translateInstantWithInterpolation.returns('Test String');

    logger = stub(Logger);

    saveDialogService = stub(SaveDialogService);
    saveDialogService.openDialog.returns(PromiseStub.resolve());

    bpmService = BpmMockUtil.mockBpmService();

    validationService = stub(ValidationService);
    validationService.init.returns(PromiseStub.resolve());
    validationService.validate.returns(PromiseStub.resolve(true));

    actionService = stub(ActionsService);

    instanceRestService = stub(InstanceRestService);

    eventbus = stub(Eventbus);

    promiseAdapter = PromiseStub;

    bpmTransitionHandler = new BpmTransitionAction(actionService, validationService, saveDialogService, instanceRestService, eventbus, logger, promiseAdapter, bpmService, translateService, notificationService);

    bpmTransitionHandler.generateInstance = (prop, instance) => {
      return mockInstance(prop, instance);
    };
    context = {
      currentObject: {
        getId: () => {
          return 'emf:id';
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

  describe('executeTransition()', () => {
    it('should notify on transition failure', () => {
      translateService.translateInstant.returnsArg(0);
      bpmService = mockErrorBpmService();
      let bpmErrorTransitionHandler = new BpmTransitionAction(actionService, validationService, saveDialogService, instanceRestService, eventbus, logger, promiseAdapter, bpmService, translateService, notificationService);
      let models = {};
      bpmErrorTransitionHandler.executeTransition(context, actionDefinition, models);
      expect(translateService.translateInstant.getCall(0).args[0]).to.equal('testLabelId');
      expect(notificationService.warning.calledOnce);
      expect(notificationService.warning.getCall(0).args[0]).to.equal('testLabelId');
    });

    it('should notify on transition failure without proper response', () => {
      translateService.translateInstant.returnsArg(0);
      bpmService = mockErrorBpmServiceIncorrectResponse();
      let bpmErrorTransitionHandler = new BpmTransitionAction(actionService, validationService, saveDialogService, instanceRestService, eventbus, logger, promiseAdapter, bpmService, translateService, notificationService);
      let models = {};
      bpmErrorTransitionHandler.executeTransition(context, actionDefinition, models);
      expect(translateService.translateInstant.getCall(0).args[0]).to.equal('error.generic');
      expect(notificationService.error.calledOnce);
      expect(notificationService.error.getCall(0).args[0]).to.equal('error.generic');
    });

    it('should notify user on action failure', () => {
      let models = {};
      bpmTransitionHandler.executeTransition(context, actionDefinition, models);
      expect(notificationService.success.calledOnce);
      expect(notificationService.success.getCall(0).args[0]).to.equal('Test String created BR header<br>');
      expect(eventbus.publish.calledOnce);
    });
  });

  describe('execute()', () => {
    it('should open dialog and notify user for executed action', () => {
      bpmTransitionHandler.execute(actionDefinition, context);
      expect(saveDialogService.openDialog.calledOnce);
      expect(notificationService.success.calledOnce);
      expect(notificationService.success.getCall(0).args[0]).to.equal('Test String created BR header<br>');
    });

    it('should log and error on rejected transition ', () => {
      bpmTransitionHandler.validationService.init = () => {
        return PromiseStub.reject('error');
      };
      bpmTransitionHandler.execute(actionDefinition, context);
      expect(logger.error.calledOnce).to.be.true;
    });
  });

  it('getObjectModel should load object models', () => {
    bpmTransitionHandler.getObjectModel(context, actionDefinition).then((resultingModel) => {
      expect(resultingModel.item1.models.id).to.equal('item1');
      // should set flag that the instance is new one
      expect(resultingModel.item1.models.isNewInstance).to.be.true;
    });
  });
});

function mockErrorBpmServiceIncorrectResponse() {
  return {
    buildBPMActionPayload: sinon.spy(() => {
      return {};
    }),
    executeTransition: sinon.spy(() => {
      let response = {
        status: StatusCodes.SERVER_ERROR
      };
      return PromiseStub.reject(response);
    })
  };
}

function mockErrorBpmService() {
  return {
    buildBPMActionPayload: sinon.spy(() => {
      return {};
    }),
    executeTransition: sinon.spy(() => {
      let response = {
        status: StatusCodes.SERVER_ERROR,
        data: {
          labelId: 'testLabelId'
        }
      };
      return PromiseStub.reject(response);
    })
  };
}

function mockInstance() {
  return {
    models: {
      id: null
    },
    mergePropertiesIntoModel: () => {

    },
    hasMandatory: () => {
      return true;
    }
  };
}