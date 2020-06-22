import {IdocMocks} from 'test/idoc/idoc-mocks';
import {UpdateExistingObjectsAction} from 'idoc/template/update-existing-objects-action';
import {Logger} from 'services/logging/logger';
import {TemplateService} from 'services/rest/template-service';
import {NotificationService} from 'services/notification/notification-service';
import {TranslateService} from 'services/i18n/translate-service';
import {DialogService} from  'components/dialog/dialog-service';
import {PromiseStub} from 'test/promise-stub';
import {InstanceObject} from 'models/instance-object';
import {stub} from 'test/test-utils';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';

describe('UpdateExistingObjectsAction', () => {

  var action;
  var currentObject;
  var templateService;
  var translateService;
  var notificationService;

  beforeEach(() => {
    var dialogService = {
      confirmation: sinon.spy()
    };

    templateService = stub(TemplateService);
    templateService.updateInstanceTemplate.returns(PromiseStub.resolve());

    notificationService = stub(NotificationService);
    translateService = stub(TranslateService);

    action = new UpdateExistingObjectsAction(stub(Logger), templateService, notificationService, translateService, dialogService, PromiseAdapterMock.mockImmediateAdapter());

    currentObject = new InstanceObject('testObject');
  });

  it('should show a confirmation message before initiating update', () => {
    action.execute({
      action: 'updateInstanceTemplateAction'
    }, {
      currentObject: currentObject,
      idocContext: {}
    });
    expect(action.dialogService.confirmation.called).to.be.true;
  });

  it('should call the rest service with proper data and notify for success after confirming', () => {
    action.execute({
      action: 'updateInstanceTemplateAction'
    }, {
      currentObject: currentObject,
      idocContext: {}
    });

    var dialogConfig = action.dialogService.confirmation.getCall(0).args[2];
    var dismissSpy = sinon.spy();
    dialogConfig.onButtonClick(DialogService.YES, undefined, {dismiss: dismissSpy});

    expect(dismissSpy.called).to.be.true;
    expect(templateService.updateInstanceTemplate.getCall(0).args[0]).to.equal(currentObject.getId());
    expect(notificationService.success.called).to.be.true;
  });

  it('should not call the rest service if confirmation is cancelled', () => {
    action.execute({
      action: 'updateInstanceTemplateAction'
    }, {
      currentObject: currentObject,
      idocContext: {}
    });

    var dialogConfig = action.dialogService.confirmation.getCall(0).args[2];
    var dismissSpy = sinon.spy();
    dialogConfig.onButtonClick(DialogService.NO, undefined, {dismiss: dismissSpy});

    expect(dismissSpy.called).to.be.true;
    expect(templateService.updateInstanceTemplate.called).to.be.false;
    expect(notificationService.success.called).to.be.false;
  });
});