import {CreateTemplateAction} from 'idoc/template/create-template-action';
import {TEMPLATE_DEFINITION_TYPE} from 'idoc/template/template-constants';
import {TemplateConfigDialogService} from 'idoc/template/template-config-dialog-service';
import {DefinitionService} from 'services/rest/definition-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {SessionStorageService} from 'services/storage/session-storage-service';
import {Router} from 'adapters/router/router';
import {PromiseStub} from 'test/promise-stub';
import {InstanceObject} from 'models/instance-object';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {stub} from 'test/test-utils';

describe('CreateTemplateAction', () => {

  it('should open template data dialog providing the type of the current instance as filter', () => {
    var templateConfigDialogService = stub(TemplateConfigDialogService);
    templateConfigDialogService.openDialog.returns(PromiseStub.reject());

    const SCOPE = 'scope';
    const OBJECT_ID = 'typeFilter';

    var action = new CreateTemplateAction(templateConfigDialogService, null);

    var currentObject = new InstanceObject(OBJECT_ID);

    var actionContext = {
      scope: SCOPE,
      currentObject: currentObject
    };

    action.execute(null, actionContext);

    expect(templateConfigDialogService.openDialog.calledOnce).to.be.true;
    expect(templateConfigDialogService.openDialog.getCall(0).args[0]).to.equal(SCOPE);
    expect(templateConfigDialogService.openDialog.getCall(0).args[1]).to.equal(null);
    expect(templateConfigDialogService.openDialog.getCall(0).args[2]).to.equal(OBJECT_ID, 'type filter not provided');
  });

  it('should fetch models and navigate to idoc page passing the models', () => {
    const OBJECT_ID = 'typeFilter';
    const SCOPE = 'scope';

    const SELECTED_TYPE = 'test_type';

    var templateData = {
      forType: SELECTED_TYPE
    };

    var templateConfigDialogService = stub(TemplateConfigDialogService);
    templateConfigDialogService.openDialog.withArgs(SCOPE, null, OBJECT_ID).returns(PromiseStub.resolve(templateData));

    var definitionData = createModel();

    var definitionServiceResponse = {
      data: {
        [TEMPLATE_DEFINITION_TYPE]: definitionData
      }
    };

    var definitionService = stub(DefinitionService);
    definitionService.getDefinitions.withArgs(TEMPLATE_DEFINITION_TYPE).returns(PromiseStub.resolve(definitionServiceResponse));

    var defaults = createDefaults();
    var instanceService = stub(InstanceRestService);
    instanceService.loadDefaults.withArgs(TEMPLATE_DEFINITION_TYPE).returns(PromiseStub.resolve({
      data: createDefaults()
    }));

    var sessionStorageService = stub(SessionStorageService);
    var router = stub(Router);

    var CURRENT_URL = 'currentUrl';
    var windowAdapter = {};

    windowAdapter.location = {
        href: CURRENT_URL
    };

    var action = new CreateTemplateAction(templateConfigDialogService, definitionService, instanceService, sessionStorageService, router, PromiseStub, windowAdapter);

    var currentObject = new InstanceObject(OBJECT_ID);

    var actionContext = {
      currentObject: currentObject,
      scope: SCOPE
    };

    action.execute(null, actionContext);

    expect(definitionData.validationModel.forObjectType.value).to.equal(SELECTED_TYPE, 'The template data should be copied to the models');

    expect(router.navigate.calledOnce).to.be.true;
    expect(router.navigate.getCall(0).args[0]).to.equal('idoc');

    expect(sessionStorageService.set.calledOnce).to.be.true;
    expect(sessionStorageService.set.getCall(0).args[0]).to.equal('models');

    var storedModel = sessionStorageService.set.getCall(0).args[1]

    expect(storedModel.headers).to.eql(defaults.headers);
    expect(storedModel.instanceType).to.eql(defaults.instanceType);
    expect(storedModel.definitionLabel).to.eql(definitionData.definitionLabel);
    expect(storedModel.definitionId).to.eql(TEMPLATE_DEFINITION_TYPE);

    expect(storedModel.validationModel['status'].value).to.equal('Default');

    expect(storedModel.validationModel['forObjectType'].value).to.equal(templateData.forType);
    expect(storedModel.validationModel['title'].value).to.equal(templateData.title);
    expect(storedModel.validationModel['templatePurpose'].value).to.equal(templateData.purpose);
    expect(storedModel.validationModel['isPrimaryTemplate'].value).to.equal(templateData.primary);

    expect(storedModel.parentId).to.equal(OBJECT_ID,'The template should be created as child of a library');

    expect(storedModel.returnUrl).to.equal(CURRENT_URL, 'Return url should be set in the model');
  });

});

function createDefaults() {
  return {
    id: 'test123',
    instanceType: 'someType',
    headers: {
      default: 'default_header'
    },
    properties: {
      'status': 'Default'
    }
  };
}

function createModel() {
  return {
    definitionLabel: 'Template',
    validationModel: {
      forObjectType: {},
      title: {},
      templatePurpose: {},
      isPrimaryTemplate: {},
      status: {}
    },
    viewModel: {
      fields: [
        {
          identifier: 'forObjectType'
        },
        {
          identifier: 'title'
        },
        {
          identifier: 'templatePurpose'
        },
        {
          identifier: 'isPrimaryTemplate'
        },
        {
          identifier: 'status'
        }
      ]
    }
  };
}