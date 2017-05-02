import {BpmStopAction} from 'idoc/actions/bpm-stop-action';
import {BpmMockUtil} from 'test/idoc/actions/bpm-mock-util';

describe('BpmStopAction ', () => {

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

    bpmTransitionHandler = new BpmStopAction(actionService, validationService, saveDialogService, instanceRestService, eventbus, logger, promiseAdapter, bpmService, translateService, notificationService);
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
      action: 'bpmStop'
    };
  });

  it('stop workflow ', () => {
    let models = {};
    bpmTransitionHandler.executeTransition(context, actionDefinition, models);
    expect(bpmService.stopBpm.calledOnce).to.be.true;
  });
});
