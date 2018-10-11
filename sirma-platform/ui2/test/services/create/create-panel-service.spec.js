import {CreatePanelService, CREATE_INSTANCE_EXTENSION_POINT} from 'services/create/create-panel-service';
import {SessionStorageService} from 'services/storage/session-storage-service';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModel} from 'models/definition-model';
import {InstanceCreatedEvent} from 'idoc/events/instance-created-event';
import {BeforeIdocSaveEvent} from 'idoc/actions/events/before-idoc-save-event';
import {PromiseStub} from 'test/promise-stub';
import {ModelsService} from 'services/rest/models-service';
import {
  SELECTION_MODE_BOTH, SELECTION_MODE_IN_CONTEXT,
  SELECTION_MODE_WITHOUT_CONTEXT
} from 'components/contextselector/context-selector';

describe('CreatePanelService', function () {

  var createInstanceAction;
  var extensionsDialogService;
  var dialogService;
  var router;
  var storage;
  var windowAdapter;
  var configurations;
  var instanceService;
  var eventbus;
  var config;

  beforeEach(() => {
    extensionsDialogService = {
      openDialog: sinon.spy()
    };
    dialogService = {
      create: sinon.spy()
    };
    router = {
      navigate () {}
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

    config = {
      models: {
        viewModel: {},
        definitionId: {},
        validationModel: {}
      },
      dialogConfig: {},
      suggestedPropertiesMap: new Map()
    };

    eventbus = mockEventbus();
    instanceService = mockInstanceService();
    createInstanceAction = new CreatePanelService(extensionsDialogService, router, storage, windowAdapter, configurations, instanceService, eventbus, dialogService);
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

  describe('openUploadInstanceDialog()', () => {
    it('should open upload dialog', () => {
      let params = {
        parentId: 'emf:123456',
        operation: 'create',
        fileObject: {}
      };

      createInstanceAction.openUploadInstanceDialog(params);
      expect(dialogService.create.callCount).to.equal(1);
      let request = dialogService.create.getCall(0).args[1];
      expect(request.config['parentId']).to.eql('emf:123456');
      expect(request.config['fileObject']).to.eql({});
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
        validationModel: new InstanceModel({})
      }, dialogConfig);


      expect(spyNavigate.callCount).to.equal(1);
      var args = spyNavigate.args[0];
      expect(args[0]).to.equal('idoc');
      expect(args[1]).to.deep.equal({
        id: undefined,
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
      // view and validation models are wrapped here
      // in their respective wrapper classes
      var models = {
        definitionId: 'definition',
        validationModel: new InstanceModel({}),
        viewModel: new DefinitionModel({
          fields: [{
            testField: {}
          }]
        })
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

    it('should publish events on successful creation', ()=> {
      // view and validation models are wrapped here
      // in their respective wrapper classes
      var models = {
        definitionId: 'definition',
        validationModel: new InstanceModel({}),
        viewModel: new DefinitionModel({
          fields: [{
            testField: {}
          }]
        })
      };
      var suggestedProperties = new Map();
      createInstanceAction.createButtonHandler(models, {}, suggestedProperties);
      expect(eventbus.publish.calledTwice).to.be.true;
      expect(eventbus.publish.getCall(0).args[0]).to.be.instanceof(BeforeIdocSaveEvent);
      expect(eventbus.publish.getCall(1).args[0]).to.be.instanceof(InstanceCreatedEvent);
    });
  });

  describe('#assignSuggestedPropertiesValues', function () {
    it('should populate property value with suggested value', ()=> {
      let models = {
        definitionId: 'definition',
        validationModel: {
          references: {
            value: {}
          }
        }
      };
      let suggestedProperties = new Map();
      suggestedProperties.set('references', [{id: 'emf:123'}]);
      createInstanceAction.assignSuggestedPropertiesValues(models, suggestedProperties);

      expect(models.validationModel.references.value).to.eql({
        results: ['emf:123'],
        add: ['emf:123'],
        remove: [],
        total: 1,
        headers: {}
      });
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
    it('should configure default header and help target', function () {
      var config = createInstanceAction.getDialogConfig();
      expect(config.header).to.exist;
      expect(config.helpTarget).to.exist;
    });

    it('should configure header and help target from provided config', function () {
      var config = createInstanceAction.getDialogConfig({
        header: 'Dialog header',
        helpTarget: 'Dialog help'
      });
      expect(config.header).to.equals('Dialog header');
      expect(config.helpTarget).to.equals('Dialog help');
    });

    it('should not close if clicked outside', function () {
      var config = createInstanceAction.getDialogConfig();
      expect(config.backdrop).to.equal('static');
    });
  });

  describe('#getModelsObject', function () {
    it('should copy the model with enabled context selector (default scenario)', function () {
      let eventEmitter = {};
      var models = CreatePanelService.getModelsObject({
        returnUrl: 'emf',
        parentId: '123',
        contextSelectorSelectionMode: SELECTION_MODE_BOTH,
        eventEmitter
      });
      expect(models).to.deep.equal({
        parentId: '123',
        returnUrl: 'emf',
        definitionId: null,
        viewModel: null,
        validationModel: null,
        contextSelectorDisabled: false,
        eventEmitter,
        contextSelectorSelectionMode: SELECTION_MODE_BOTH
      });
    });
    it('should copy the model with enabled context selector (scenario enabled in configuration)', function () {
      let eventEmitter = {};
      var models = CreatePanelService.getModelsObject({
        returnUrl: 'emf',
        parentId: '123',
        contextSelectorDisabled: false,
        eventEmitter,
        contextSelectorSelectionMode: SELECTION_MODE_IN_CONTEXT
      });
      expect(models).to.deep.equal({
        parentId: '123',
        returnUrl: 'emf',
        definitionId: null,
        viewModel: null,
        validationModel: null,
        contextSelectorDisabled: false,
        eventEmitter,
        contextSelectorSelectionMode: SELECTION_MODE_IN_CONTEXT
      });
    });
    it('should copy the model with disabled context selector (scenario disabled in configuration)', function () {
      let eventEmitter = {};
      var models = CreatePanelService.getModelsObject({
        returnUrl: 'emf',
        parentId: '123',
        contextSelectorDisabled: true,
        eventEmitter,
        contextSelectorSelectionMode: SELECTION_MODE_WITHOUT_CONTEXT
      });
      expect(models).to.deep.equal({
        parentId: '123',
        returnUrl: 'emf',
        definitionId: null,
        viewModel: null,
        validationModel: null,
        contextSelectorDisabled: true,
        eventEmitter,
        contextSelectorSelectionMode: SELECTION_MODE_WITHOUT_CONTEXT
      });
    });
  });

  describe('#getUploadPanelConfig', function () {

    it('should return proper file upload panel configuration', function () {
      var properties = createInstanceAction.getUploadPanelConfig({
        parentId: 'parent1',
        controls: {
          showCancelButton: false
        },
        predefinedTypes: 'predefinedTypes',
        predefinedSubTypes: 'predefinedSubTypes'
      }, config);

      expect(properties.config.parentId).to.equals('parent1');
      expect(properties.config.purpose).to.deep.equal([ModelsService.PURPOSE_UPLOAD]);
      expect(properties.config.classFilter).to.equals('predefinedTypes');
      expect(properties.config.definitionFilter).to.equals('predefinedSubTypes');
      expect(properties.config.controls).to.deep.equal({
        showCancelButton: false
      });
      expect(properties.config.onCancel).to.exist;
    });
  });

  describe('#getCreatePanelConfig', function () {

    it('should return properties that contain the model and parent instance id', function () {
      var properties = createInstanceAction.getCreatePanelConfig({
        parentId: 'parent1',
        operation: 'create'
      }, config);
      expect(properties.config.parentId).to.equals('parent1');
      expect(properties.config.renderMandatory).to.be.true;
      expect(properties.config.operation).to.equals('create');
      expect(properties.config.formConfig).to.deep.equal({
        models: {
          viewModel: {},
          definitionId: {},
          validationModel: {}
        }
      });
      expect(properties.config.onCreate).to.exist;
      expect(properties.config.onOpen).to.exist;
      expect(properties.config.onOpenInNewTab).to.exist;
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

      var properties = createInstanceAction.getCreatePanelConfig({
        createButtonLabel: 'my.label',
        suggestProperties: true,
        predefinedTypes: [],
        forceCreate: false,
        controls: {
          showCreate: true,
          showCancel: false,
          showCreateMore: true
        },
        instanceData,
        instanceType: 'emd:Document',
        instanceSubType: 'SUBTYPE'
      }, config);

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
      expect(properties.config.purpose).to.deep.equal([ModelsService.PURPOSE_CREATE]);
      expect(properties.config.instanceType).to.equal('emd:Document');
      expect(properties.config.instanceSubType).to.equal('SUBTYPE');
    });

    it('should configure default onCreate() function', () => {
      createInstanceAction.createButtonHandler = sinon.spy();
      var properties = createInstanceAction.getCreatePanelConfig({}, config);
      expect(properties.config.onCreate).to.exist;
      properties.config.onCreate();
      expect(createInstanceAction.createButtonHandler.calledOnce).to.be.true;
    });

    it('should provide external onCreate function', () => {
      var onCreateSpy = sinon.spy();
      var properties = createInstanceAction.getCreatePanelConfig({
        onCreate: onCreateSpy
      }, config);
      expect(properties.config.onCreate).to.exist;
      properties.config.onCreate();
      expect(onCreateSpy.calledOnce).to.be.true;
    });

    it('should configure instance created callback when passed', function () {
      var properties = createInstanceAction.getCreatePanelConfig({
        parentId: 'parent1',
        operation: 'create',
        instanceCreatedCallback: () => {}
      }, config);

      expect(properties.config.instanceCreatedCallback).to.exist;
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
}

function mockEventbus() {
  return {
    publish: sinon.spy()
  };
}
