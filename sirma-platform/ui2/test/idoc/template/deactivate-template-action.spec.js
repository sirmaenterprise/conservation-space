import {IdocMocks} from 'test/idoc/idoc-mocks';
import {BpmMockUtil} from 'test/idoc/actions/bpm-mock-util';
import {DeactivateTemplateAction} from 'idoc/template/deactivate-template-action';
import {PromiseStub} from 'test/promise-stub';
import {InstanceObject} from 'models/instance-object';
import {TemplateService} from 'services/rest/template-service';
import {stub} from 'test/test-utils';

describe('DeactivateTemplateAction', () => {

  let action;
  let logger;
  let templateService;
  let notificationService;
  let translateService;

  beforeEach(() => {
    logger = IdocMocks.mockLogger();
    templateService = stub(TemplateService);
    templateService.deactivateTemplate.returns(PromiseStub.resolve());

    notificationService = BpmMockUtil.mockNotificationService();
    translateService = BpmMockUtil.mockTranslateService();
    action = new DeactivateTemplateAction(logger, templateService, notificationService, translateService, PromiseStub);

    action.refreshInstance = sinon.stub();
  });

  it('should call rest service with proper data, notify user of successful execution and reload the entity if opened in idoc', () => {
    action.refreshInstance = sinon.stub();

    action.execute({
      action: 'deactivateTemplate'
    }, {
      currentObject: new InstanceObject('emf:id'),
      idocContext: {}
    }).then(() => {
      expect(templateService.deactivateTemplate.args[0][0]).to.equal("emf:id");
      expect(templateService.deactivateTemplate.calledOnce).to.be.true;
      expect(notificationService.success.calledOnce).to.be.true;

      expect(action.refreshInstance.calledOnce).to.be.true;
    });
  });

  it('should not reload the entity if not opened in idoc', () => {
    action.execute({
      action: 'deactivateTemplate'
    }, {
      currentObject: new InstanceObject('emf:id')
    }).then(() => {
      expect(templateService.deactivateTemplate.args[0][0]).to.equal("emf:id");

      expect(action.refreshInstance.called).to.be.false;
    });
  });

});