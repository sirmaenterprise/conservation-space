import {IdocMocks} from 'test/idoc/idoc-mocks';
import {BpmMockUtil} from 'test/idoc/actions/bpm-mock-util';
import {ActivateTemplateAction} from 'idoc/template/activate-template-action';
import {PromiseStub} from 'test/promise-stub';
import {InstanceObject} from 'models/instance-object';

describe('ActivateTemplateAction', () => {

  let action;
  let logger;
  let actionService;
  let notificationService;
  let translateService;

  beforeEach(() => {
    logger = IdocMocks.mockLogger();
    actionService = IdocMocks.mockActionsService();
    notificationService = BpmMockUtil.mockNotificationService();
    translateService = BpmMockUtil.mockTranslateService();
    action = new ActivateTemplateAction(logger, actionService, notificationService, translateService, PromiseStub);

    action.refreshInstance = sinon.stub();
  });

  it('should call rest service with proper data, notify user of successful execution and reload the entity if opened in idoc', () => {
    action.refreshInstance = sinon.stub();

    action.execute({
      action: 'activateTemplate'
    }, {
      currentObject: new InstanceObject('emf:id'),
      idocContext: {}
    }).then(() => {
      expect(actionService.activateTemplate.args[0][0]).to.equal("emf:id");
      expect(actionService.activateTemplate.args[0][1]).to.deep.equal({
        operation: 'activateTemplate',
        userOperation: 'activateTemplate'
      });
      expect(actionService.activateTemplate.calledOnce).to.be.true;
      expect(notificationService.success.calledOnce).to.be.true;

      expect(action.refreshInstance.calledOnce).to.be.true;
    });
  });

  it('should not reload the entity if not opened in idoc', () => {
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

      expect(action.refreshInstance.called).to.be.false;
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