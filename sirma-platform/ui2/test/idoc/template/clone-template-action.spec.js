import {IdocMocks} from 'test/idoc/idoc-mocks';
import {CloneTemplateAction} from 'idoc/template/clone-template-action';
import {TemplateConfigDialogService} from 'idoc/template/template-config-dialog-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {TemplateService} from 'services/rest/template-service';
import {PromiseStub} from 'test/promise-stub';
import {InstanceObject} from 'models/instance-object';
import {stub} from 'test/test-utils';
import {NotificationService} from 'services/notification/notification-service';
import {TranslateService} from 'services/i18n/translate-service';

describe('CloneTemplateAction', () => {

  var instanceService;
  var templateConfigDialogService;
  var currentObject;
  var templateService;
  var notificationService;
  var translateService;

  beforeEach(() => {
    templateConfigDialogService = stub(TemplateConfigDialogService);
    instanceService = stub(InstanceRestService);
    templateConfigDialogService.openDialog.returns(PromiseStub.reject());
    templateService = stub(TemplateService);
    notificationService = stub(NotificationService);
    translateService = stub(TranslateService);

    var models = buildTestInstanceModel();

    currentObject = new InstanceObject('testObject', models);
    instanceService.load.returns(PromiseStub.resolve({data: currentObject}));
  });

  it('should open template data dialog for cloning with correct arguments', () => {
    const SCOPE = 'scope';
    var actionContext = {
      scope: SCOPE,
      currentObject: new InstanceObject(currentObject.getId())
    };

    var action = new CloneTemplateAction(templateConfigDialogService, templateService, translateService, notificationService, instanceService);
    action.execute(null, actionContext);

    expect(templateConfigDialogService.openDialog.calledOnce).to.be.true;
    expect(templateConfigDialogService.openDialog.getCall(0).args[0]).to.equal(SCOPE);
    expect(templateConfigDialogService.openDialog.getCall(0).args[1]).to.deep.equal(currentObject);
    expect(templateConfigDialogService.openDialog.getCall(0).args[2]).toBeUndefined;
    expect(templateConfigDialogService.openDialog.getCall(0).args[3]).to.be.true;
    expect(templateConfigDialogService.openDialog.getCall(0).args[4]).to.equal(TemplateConfigDialogService.CLONE_TEMPLATE_KEY);
  });

  it('should call create rest service with the correct template data from the dialog', () => {
    const SCOPE = 'scope';
    var actionContext = {
      scope: SCOPE,
      currentObject: new InstanceObject(currentObject.getId())
    };

    templateConfigDialogService.openDialog.returns(PromiseStub.resolve(currentObject));
    templateService.create.returns({data: currentObject.getId()});

    var action = new CloneTemplateAction(templateConfigDialogService, templateService, translateService, notificationService, instanceService);
    action.execute(null, actionContext);

    expect(templateService.create.calledOnce).to.be.true;
    expect(templateService.create.getCall(0).args[0]).to.equal(currentObject);
  });

  it('should notify the user for successful operation when template was cloned', () => {
    const SCOPE = 'scope';
    var actionContext = {
      scope: SCOPE,
      currentObject: new InstanceObject(currentObject.getId())
    };
    const SUCCESS_MESSAGE = 'sucess message';
    translateService.translateInstant.withArgs('idoc.template.clone.success').returns(SUCCESS_MESSAGE);

    templateConfigDialogService.openDialog.returns(PromiseStub.resolve(currentObject));
    templateService.create.returns({data: currentObject.getId()});

    var action = new CloneTemplateAction(templateConfigDialogService, templateService, translateService, notificationService, instanceService);
    action.execute(null, actionContext);

    expect(notificationService.success.calledOnce).to.be.true;
  });
});

function buildTestInstanceModel() {
  return {
    viewModel: {
      fields: [{
        identifier: 'forObjectType'
      }, {
        identifier: 'templatePurpose'
      }, {
        identifier: 'isPrimaryTemplate'
      }, {
        identifier: 'title'
      }]
    },
    validationModel: {
      'forObjectType': {
        value: 'sampleType'
      },
      'templatePurpose': {
        value: 'creatable'
      },
      'isPrimaryTemplate': {
        value: true
      },
      'title': {
        value: 'Sample title'
      }
    }
  }
}