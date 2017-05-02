import {IdocMocks} from 'test/idoc/idoc-mocks';
import {BpmMockUtil} from 'test/idoc/actions/bpm-mock-util';
import {ActivateTemplateAction} from 'idoc/actions/activate-template-action';
import {PromiseStub} from 'test/promise-stub';
import {InstanceObject} from 'idoc/idoc-context';

describe('ActivateTemplateAction', () => {

  let action;
  let logger;
  let actionService;
  let notificationService;
  let translateService;

  beforeEach(() => {
    logger = IdocMocks.mockLogger();
    actionService = IdocMocks.mockActionsService();
    ActivateTemplateAction.prototype.refreshInstance = () => {

    };
    notificationService = BpmMockUtil.mockNotificationService();
    translateService = BpmMockUtil.mockTranslateService();
    action = new ActivateTemplateAction(logger, actionService, notificationService, translateService);
  });

  it('should call rest service with proper data and notify user of successful execution', () => {
    action.execute({
      action: 'activateTemplate'
    }, {
      currentObject: new InstanceObject('emf:id')
    }).then(() => {
      expect(actionService.activateTemplate.args[0][0]).to.equal("emf:id");
      expect(actionService.activateTemplate.args[0][1]).to.deep.equal({
        operation: 'activateTemplate',
        userOperation: 'activateTemplate'
      });
      expect(actionService.activateTemplate.calledOnce).to.be.true;
      expect(notificationService.success.calledOnce).to.be.true;
    });
  });

  it('should handle error of rest service', () => {
    actionService.activateTemplate = () => {
      return PromiseStub.reject();
    };
    action.execute({
      action: "activateTemplate"
    }, {
      currentObject: new InstanceObject('emf:id')
    }).then(() => {
      expect(notificationService.error.calledOnce).to.be.true;
    });
  });

});