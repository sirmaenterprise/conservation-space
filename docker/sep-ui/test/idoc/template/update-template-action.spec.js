import {UpdateTemplateAction} from 'idoc/template/update-template-action';
import {TemplateService} from 'services/rest/template-service';
import {NotificationService} from 'services/notification/notification-service';
import {TranslateService} from 'services/i18n/translate-service';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';
import {PromiseStub} from 'test/promise-stub';
import {STATE_PARAM_ID, IDOC_PAGE_ACTIONS_PLACEHOLDER, STATE_PARAM_MODE} from 'idoc/idoc-constants';
import {StateParamsAdapterMock} from 'test/adapters/angular/state-params-adapter-mock';
import {InstanceObject} from 'models/instance-object';
import {InstanceRestService} from 'services/rest/instance-service';
import {stub} from 'test/test-utils';

describe('UpdateTemplateAction', () => {

  var action;
  var templateService;
  var notificationService;
  var translateService;
  var promiseAdapter;
  var router;
  var stateParamsAdapter;
  var currentObject;
  var instanceService;

  beforeEach(() => {

    templateService = stub(TemplateService);
    templateService.updateSingleInstanceTemplate.returns(PromiseStub.resolve({status: 200}));
    templateService.getActualTemplateVersion.returns(PromiseStub.resolve({data: '1.19'}));

    notificationService = stub(NotificationService);
    translateService = stub(TranslateService);

    promiseAdapter = PromiseAdapterMock.mockImmediateAdapter();
    router = {
      navigate: sinon.spy()
    };
    stateParamsAdapter = StateParamsAdapterMock.mockAdapter();
    instanceService = stub(InstanceRestService);

    action = new UpdateTemplateAction(templateService, notificationService, translateService, promiseAdapter, router, stateParamsAdapter, instanceService);
    currentObject = new InstanceObject('testObject', generateModels());
  });

  it('should update instance template when no template version is set', () => {
    action.execute({
      action: 'updateTemplateAction'
    }, {
      currentObject: currentObject,
      placeholder: IDOC_PAGE_ACTIONS_PLACEHOLDER
    });
    expect(templateService.updateSingleInstanceTemplate.called).to.be.true;
  });

  it('should not update instance template when no template is set', () => {
    currentObject.setPropertiesValue({
      'emf:hasTemplate': undefined
    });

    action.execute({
      action: 'updateTemplateAction'
    }, {
      currentObject: currentObject,
      placeholder: IDOC_PAGE_ACTIONS_PLACEHOLDER
    });

    expect(notificationService.warning.called).to.be.true;
    expect(templateService.updateSingleInstanceTemplate.called).to.be.false;
  });

  it('should show a warning message when no content is returned from backend', () => {
    templateService.updateSingleInstanceTemplate.returns(PromiseStub.resolve({status: 204}));

    action.execute({
      action: 'updateTemplateAction'
    }, {
      currentObject: currentObject,
      placeholder: IDOC_PAGE_ACTIONS_PLACEHOLDER
    });

    expect(templateService.updateSingleInstanceTemplate.called).to.be.true;
    expect(notificationService.warning.called).to.be.true;
  });

  it('should show a warning message when instance is up to date', () => {
    currentObject.setPropertiesValue({
      'emf:templateVersion': '1.19'
    });
    action.execute({
      action: 'updateTemplateAction'
    }, {
      currentObject: currentObject,
      placeholder: IDOC_PAGE_ACTIONS_PLACEHOLDER
    });
    expect(notificationService.warning.called).to.be.true;
    expect(templateService.updateSingleInstanceTemplate.called).to.be.false;
  });

  it('should show a success message when instance is updated', () => {
    currentObject.setPropertiesValue({
      'emf:templateVersion': '1.10'
    });
    action.execute({
      action: 'updateTemplateAction'
    }, {
      currentObject: currentObject,
      placeholder: IDOC_PAGE_ACTIONS_PLACEHOLDER
    });
    expect(notificationService.success.called).to.be.true;
    expect(templateService.updateSingleInstanceTemplate.called).to.be.true;
  });

  it('should reolad instance when instance is updated from idoc page actions placeholder', () => {
    let expectedStateParams = {};
    expectedStateParams[STATE_PARAM_ID] = 'testObject';
    expectedStateParams[STATE_PARAM_MODE] = 'preview';
    let expectedOptions = {
      reload: true
    };

    currentObject.setPropertiesValue({
      'emf:templateVersion': '1.10'
    });
    action.execute({
      action: 'updateTemplateAction'
    }, {
      currentObject: currentObject,
      placeholder: IDOC_PAGE_ACTIONS_PLACEHOLDER
    });

    expect(templateService.updateSingleInstanceTemplate.called).to.be.true;
    expect(action.router.navigate.called).to.be.true;
    expect(action.router.navigate.getCall(0).args[0]).to.equal('idoc');
    expect(action.router.navigate.getCall(0).args[1]).to.deep.equal(expectedStateParams);
    expect(action.router.navigate.getCall(0).args[2]).to.deep.equal(expectedOptions);
    expect(notificationService.success.called).to.be.true;
  });

  it('should update instance template when called outside idoc', () => {
    instanceService.loadBatch.returns(PromiseStub.resolve({
      data: [{
        properties: {
          'emf:hasTemplate': {results: ['emf123']},
          'emf:templateVersion': '1.1'
        }
      }]
    }));

    action.execute({
      action: 'updateTemplateAction'
    }, {
      currentObject: currentObject
    });
    expect(instanceService.loadBatch.called).to.be.true;
    expect(templateService.updateSingleInstanceTemplate.called).to.be.true;
  });
});

function generateModels() {
  return {
    viewModel: {
      fields: []
    },
    validationModel: {
      'emf:templateVersion': {
        defaultValue: '',
        value: ''
      },
      'emf:hasTemplate': {
        defaultValue: '',
        value: 'emf:123'
      }
    }
  };
}