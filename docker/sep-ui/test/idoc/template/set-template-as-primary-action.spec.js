import {IdocMocks} from 'test/idoc/idoc-mocks';
import {SetTemplateAsPrimaryAction} from 'idoc/template/set-template-as-primary-action';
import {Logger} from 'services/logging/logger';
import {TemplateService} from 'services/rest/template-service';
import {NotificationService} from 'services/notification/notification-service';
import {TranslateService} from 'services/i18n/translate-service';
import {PromiseStub} from 'test/promise-stub';
import {InstanceObject} from 'models/instance-object';
import {stub} from 'test/test-utils';

describe('SetTemplateAsPrimaryAction', () => {

  var action;
  var currentObject;
  var templateService;
  var translateService;
  var notificationService;

  beforeEach(() => {
    templateService = stub(TemplateService);
    templateService.setTemplateAsPrimary.returns(PromiseStub.resolve());

    notificationService = stub(NotificationService);
    translateService = stub(TranslateService);

    action = new SetTemplateAsPrimaryAction(stub(Logger), templateService, notificationService, translateService, PromiseStub);
    action.refreshInstance = sinon.stub();

    currentObject = new InstanceObject('testObject');
  });

  it('should call rest service with proper data, notify user of successful execution and reload the entity if opened in idoc', () => {
    const SUCCESS_MESSAGE = 'sucess message';

    translateService.translateInstant.withArgs('idoc.template.set_as_primary.success').returns(SUCCESS_MESSAGE);

    action.execute({
      action: 'setTemplateAsPrimary'
    }, {
      currentObject: currentObject,
      idocContext: {}
    }).then(() => {
      expect(templateService.setTemplateAsPrimary.getCall(0).args[0]).to.equal(currentObject.getId());

      expect(notificationService.success.getCall(0).args[0]).to.equal(SUCCESS_MESSAGE,'The user should be notified on successful operation');

      expect(action.refreshInstance.called).to.be.true;
    });
  });

  it('should not reload the entity if not opened in idoc', () => {
    action.execute({
      action: 'setTemplateAsPrimary'
    }, {
      currentObject: currentObject
    }).then(() => {
      expect(templateService.setTemplateAsPrimary.getCall(0).args[0]).to.equal(currentObject.getId());

      expect(action.refreshInstance.called).to.be.false;
    });
  });

});