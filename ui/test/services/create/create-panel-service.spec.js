import {CreatePanelService, CREATE_INSTANCE_EXTENSION_POINT} from 'services/create/create-panel-service';
import {SessionStorageService} from 'services/storage/session-storage-service';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModel} from 'models/definition-model';
import {InstanceCreatedEvent} from 'idoc/events/instance-created-event';
import {PromiseStub} from 'test/promise-stub';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';

describe('CreateInstanceAction', function () {

  var createInstanceAction;
  var extensionsDialogService;
  var router;
  var storage;
  var windowAdapter;
  var configurations;
  var instanceService;
  var eventbus;

  beforeEach(() => {
    extensionsDialogService = {
      openDialog: sinon.spy()
    };
    router = {
      navigate: function () {
      }
    };
    storage = new SessionStorageService(window);
    windowAdapter = {
      location: {
        href: 'href-returnUrl'
      }
    };
    configurations = {
      get: ()=> {
      }
    };
    eventbus = mockEventbus();
    instanceService = mockInstanceService();
    createInstanceAction = new CreatePanelService(extensionsDialogService, router, storage, windowAdapter, configurations, instanceService, eventbus);
  });

  describe('openCreateInstanceDialog()', () => {
    it('should open extensions dialog', () => {
      createInstanceAction.openCreateInstanceDialog({});
      expect(extensionsDialogService.openDialog.callCount).to.equal(1);
    });

    it('should construct extensions configurations', () => {
      configurations.get = ()=> {
        return 'max-size';
      };
      var opts = {
        parentId: 'GEP11111',
        operation: 'create',
        returnUrl: 'returnUrl'
      };
      createInstanceAction.openCreateInstanceDialog(opts);

      var extensionsConfig = extensionsDialogService.openDialog.getCall(0).args[0];
      expect(extensionsConfig.extensionPoint).to.equal(CREATE_INSTANCE_EXTENSION_POINT);
      expect(extensionsConfig.extensions['instance-create-panel']).to.exist;
      expect(extensionsConfig.extensions['file-upload-panel']).to.exist;
      expect(extensionsConfig.extensions['file-upload-panel'].maxFileSize).to.equal('max-size');
      expect(extensionsConfig.extensions['file-upload-panel'].onCancel).to.exist;
    });
  });

  describe('#openButtonHandler', function () {
    it('should set the models in the storage and navigate to idoc', function () {
      var spyNavigate = sinon.spy(router, 'navigate');
      var dialogConfig = {
        dismiss: ()=> {
        }
      };
      //validaiton model is passed as an instance model class from the instance object.
      createInstanceAction.openButtonHandler({
        viewModel: new DefinitionModel({}),
        validationModel: new InstanceModel({}),
      }, dialogConfig);

      expect(storage.getJson('models')).to.deep.equal({
        viewModel: {},
        validationModel: {}
      });
      expect(spyNavigate.callCount).to.equal(1);
      var args = spyNavigate.args[0];
      expect(args[0]).to.equal('idoc');
      expect(args[1]).to.deep.equal({
        mode: 'edit'
      });
    });

    it('should dismiss the dialog', ()=> {
      var spyDismiss = {
        dismiss: sinon.spy()
      };
      createInstanceAction.openButtonHandler({}, spyDismiss);
      expect(spyDismiss.dismiss.calledOnce).to.be.true;
    });
  });

  describe('#createButtonHandler', function () {
    it('should create object and return it', ()=> {
      var models = {
        definitionId: 'definition',
        validationModel: {},
        viewModel: {
          fields: [{
            testField: {}
          }]
        }
      };
      var suggestedProperties = new Map();
      var result = createInstanceAction.createButtonHandler(models, {}, suggestedProperties);
      expect(instanceService.create.calledOnce).to.be.true;
      expect(result).to.eventually.deep.equal({
        headers: {
          breadcrumb_header: 'header'
        }
      });
    });

    it('should publish event on successful creation', ()=> {
      var models = {
        definitionId: 'definition',
        validationModel: {},
        viewModel: {
          fields: [{
            testField: {}
          }]
        }
      };
      var suggestedProperties = new Map();
      var result = createInstanceAction.createButtonHandler(models, {}, suggestedProperties);
      expect(eventbus.publish.calledOnce).to.be.true;
      expect(eventbus.publish.getCall(0).args[0]).to.be.instanceof(InstanceCreatedEvent);
    });
  });

  describe('#assignSuggestedPropertiesValues', function () {
    it('should populate property value with suggested value', ()=> {
      let models = {
        definitionId: 'definition',
        validationModel: {
          references: {
            value: undefined
          }
        }
      };
      let suggestedProperties = new Map();
      suggestedProperties.set('references', [{id: 'emf:123'}]);
      createInstanceAction.assignSuggestedPropertiesValues(models, suggestedProperties);

      expect(models.validationModel.references.value).to.deep.equal([{id: 'emf:123'}]);
    });

    it('should concatenate property value with suggested value if its array', ()=> {
      let models = {
        definitionId: 'definition',
        validationModel: {
          references: {
            value: [{id: 'emf:123'}]
          }
        }
      };
      let suggestedProperties = new Map();
      suggestedProperties.set('references', [{id: 'emf:321'}]);
      createInstanceAction.assignSuggestedPropertiesValues(models, suggestedProperties);

      expect(models.validationModel.references.value).to.deep.equal([{id: 'emf:123'}, {id: 'emf:321'}]);
    });
  });

  describe('#cancelButtonHandler', function () {
    it('should navigate back to old url if provided', function () {
      var dialogConfig = {
        dismiss: ()=> {
        }
      };
      createInstanceAction.cancelButtonHandler({
        viewModel: {},
        validationModel: {},
        returnUrl: 'emf'
      }, dialogConfig);
      expect(windowAdapter.location.href).to.equal('emf');
    });

    it('should not navigate back to old url if not provided', function () {
      windowAdapter.location.href = 'my-location';
      var dialogConfig = {
        dismiss: ()=> {
        }
      };
      createInstanceAction.cancelButtonHandler({
        viewModel: {},
        validationModel: {}
      }, dialogConfig);
      expect(windowAdapter.location.href).to.equal('my-location');
    });

    it('should dismiss the dialog', ()=> {
      var spyDismiss = {
        dismiss: sinon.spy()
      };
      createInstanceAction.cancelButtonHandler({}, spyDismiss);
      expect(spyDismiss.dismiss.calledOnce).to.be.true;
    });
  });

  describe('#getDialogConfig', function () {
    it('should configure header and help target', function () {
      var config = CreatePanelService.getDialogConfig();
      expect(config.header).to.exist;
      expect(config.helpTarget).to.exist;
    });

    it('should configure header and help target from provided config', function () {
      var config = CreatePanelService.getDialogConfig({
        header: 'Dialog header',
        helpTarget: 'Dialog help'
      });
      expect(config.header).to.equals('Dialog header');
      expect(config.helpTarget).to.equals('Dialog help');
    });

    it('should not close if clicked outside', function () {
      var config = CreatePanelService.getDialogConfig();
      expect(config.backdrop).to.equal('static');
    });
  });

  describe('#getModelsObject', function () {
    it('should copy the model with enabled context selector (default scenario)', function () {
      var models = CreatePanelService.getModelsObject({
        returnUrl: 'emf',
        parentId: '123'
      });
      expect(models).to.deep.equal({
        parentId: '123',
        returnUrl: 'emf',
        definitionId: null,
        viewModel: null,
        validationModel: null,
        contextSelectorDisabled: false
      });
    });
    it('should copy the model with enabled context selector (scenario enabled in configuration)', function () {
      var models = CreatePanelService.getModelsObject({
        returnUrl: 'emf',
        parentId: '123',
        contextSelectorDisabled: false
      });
      expect(models).to.deep.equal({
        parentId: '123',
        returnUrl: 'emf',
        definitionId: null,
        viewModel: null,
        validationModel: null,
        contextSelectorDisabled: false
      });
    });
    it('should copy the model with disabled context selector (scenario disabled in configuration)', function () {
      var models = CreatePanelService.getModelsObject({
        returnUrl: 'emf',
        parentId: '123',
        contextSelectorDisabled: true
      });
      expect(models).to.deep.equal({
        parentId: '123',
        returnUrl: 'emf',
        definitionId: null,
        viewModel: null,
        validationModel: null,
        contextSelectorDisabled: true
      });
    });
  });

  describe('#getPropertiesConfig', function () {
    it('should return properties that contain the model and parent instance id', function () {
      var suggestedPropertiesMap = new Map();
      var properties = createInstanceAction.getPropertiesConfig({
        parentId: 'parent1',
        operation: 'create'
      }, {
        definitionId: null,
        viewModel: null,
        validationModel: null
      }, null, suggestedPropertiesMap);
      expect(properties.config.parentId).to.equals('parent1');
      expect(properties.config.renderMandatory).to.be.true;
      expect(properties.config.operation).to.equals('create');
      expect(properties.config.formConfig).to.deep.equal({
        models: {
          definitionId: null,
          viewModel: null,
          validationModel: null
        }
      });
      expect(properties.config.onCreate).to.exist;
      expect(properties.config.onOpen).to.exist;
      expect(properties.config.onCancel).to.exist;
      expect(properties.config.suggestedProperties).to.exist;
    });

    it('should configure predefined instance data', () => {
      var instanceData = {
        id: 'emf:123',
        properties: {
          'emf:createdBy': 'emf:admin'
        }
      };
      var properties = createInstanceAction.getPropertiesConfig({
        createButtonLabel: 'my.label',
        suggestProperties: true,
        predefinedTypes: [],
        forceCreate: false,
        controls: {
          showCreate: true,
          showCancel: false,
          showCreateMore: true
        },
        purpose: 'some-purpose',
        instanceData: instanceData,
        instanceType: 'emd:Document',
        instanceSubType: 'SUBTYPE'
      });

      expect(properties.config.instanceData).to.deep.equal(instanceData);
      expect(properties.config.createButtonLabel).to.equal('my.label');
      expect(properties.config.forceCreate).to.be.false;
      expect(properties.config.classFilter).to.deep.eq([]);
      expect(properties.config.controls).exist;
      expect(properties.config.controls).to.deep.eq({
        showCreate: true,
        showCancel: false,
        showCreateMore: true
      });
      expect(properties.config.purpose).to.equals('some-purpose');
      expect(properties.config.instanceType).to.equal('emd:Document');
      expect(properties.config.instanceSubType).to.equal('SUBTYPE');
    });

    it('should configure default onCreate() function', () => {
      createInstanceAction.createButtonHandler = sinon.spy();
      var properties = createInstanceAction.getPropertiesConfig({});
      expect(properties.config.onCreate).to.exist;
      properties.config.onCreate();
      expect(createInstanceAction.createButtonHandler.calledOnce).to.be.true;
    });

    it('should provide external onCreate function', () => {
      var onCreateSpy = sinon.spy();
      var properties = createInstanceAction.getPropertiesConfig({
        onCreate: onCreateSpy
      });
      expect(properties.config.onCreate).to.exist;
      properties.config.onCreate();
      expect(onCreateSpy.calledOnce).to.be.true;
    });
  });

  describe('#resolveContextSelectorDisabled', () => {
    it('should return default value if contextSelectorDisabled is undefined', () => {
      let config = {};
      expect(CreatePanelService.resolveContextSelectorDisabled(config, false)).to.be.false;
      expect(CreatePanelService.resolveContextSelectorDisabled(config, true)).to.be.true;
    });

    it('should return default value if contextSelectorDisabled is null', () => {
      let config = {
        contextSelectorDisabled: null
      };
      expect(CreatePanelService.resolveContextSelectorDisabled(config, false)).to.be.false;
      expect(CreatePanelService.resolveContextSelectorDisabled(config, true)).to.be.true;
    });

    it('should return false if contextSelectorDisabled is false', () => {
      let config = {
        contextSelectorDisabled: false
      };
      expect(CreatePanelService.resolveContextSelectorDisabled(config, false)).to.be.false;
      expect(CreatePanelService.resolveContextSelectorDisabled(config, true)).to.be.false;
    });

    it('should return true value if contextSelectorDisabled is true', () => {
      let config = {
        contextSelectorDisabled: true
      };
      expect(CreatePanelService.resolveContextSelectorDisabled(config, false)).to.be.true;
      expect(CreatePanelService.resolveContextSelectorDisabled(config, true)).to.be.true;
    });
  });
});

function mockInstanceService() {
  return {
    create: sinon.spy(() => {
      return PromiseStub.resolve({
        data: {
          headers: {
            breadcrumb_header: 'header'
          }
        }
      });
    })
  };
};

function mockEventbus() {
  return {
    publish: sinon.spy()
  };
};
