import {BpmStartAction} from 'idoc/actions/bpm-start-action';
import {BpmMockUtil} from 'test/idoc/actions/bpm-mock-util';

describe('BpmStartAction ', () => {

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

    bpmTransitionHandler = new BpmStartAction(actionService, validationService, saveDialogService, instanceRestService, eventbus, logger, promiseAdapter, bpmService, translateService, notificationService);
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
      action: 'bpmStart'
    };
  });

  it('notifies on success ', () => {
    let models = {};
    bpmTransitionHandler.executeTransition(context, actionDefinition, models);
    expect(notificationService.success.calledOnce);
    expect(notificationService.success.getCall(0).args[0]).to.equal('Test String created BR header<br>');
  });

});
