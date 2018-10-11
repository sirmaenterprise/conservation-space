import {ResourceManagement, GROUP} from 'administration/resources-management/resource-management';
import {ResourceRestService} from 'services/rest/resources-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {SaveDialogService} from 'idoc/save-idoc-dialog/save-dialog-service';
import {CreatePanelService} from 'services/create/create-panel-service';
import {NamespaceService} from 'services/rest/namespace-service';
import {Configuration} from 'common/application-config';
import {ModelsService} from 'services/rest/models-service';
import {InstanceObject} from 'models/instance-object';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('ResourceManagement', () => {

  let resourceManagement;
  let resourceService;
  let instanceService;
  let saveDialogService;
  let createPanelService;
  let namespaceService;

  beforeEach(() => {
    resourceService = stubResourceService();
    instanceService = stubInstanceService();
    saveDialogService = stubSaveDialogService();
    namespaceService = stubNamespaceService();
    createPanelService = stub(CreatePanelService);

    resourceManagement = new ResourceManagement(resourceService, instanceService, saveDialogService, createPanelService, namespaceService, PromiseStub);
    resourceManagement.initConfig();
  });

  describe('initConfig', () => {
    it('should configure user management by default', () => {
      expect(resourceManagement.propertiesConfigKey).to.equal(Configuration.USER_MANAGEMENT_USER_PROPERTIES);
      expect(resourceManagement.headerLabel).to.equal('user.management.table.header');
      expect(resourceManagement.createLabel).to.equal('user.management.create');

      resourceManagement.resourceLoader(1, 10, ['id']);

      expect(resourceService.getAllUsers.calledWith(1, 10, ['id'])).to.be.true;
    });

    it('should configure group management when resource type is group', () => {
      resourceManagement.config.resourceType = GROUP;
      resourceManagement.initConfig();

      expect(resourceManagement.propertiesConfigKey).to.equal(Configuration.GROUP_MANAGEMENT_GROUP_PROPERTIES);
      expect(resourceManagement.headerLabel).to.equal('group.management.table.header');
      expect(resourceManagement.createLabel).to.equal('group.management.create');

      resourceManagement.resourceLoader(1, 10, ['id']);

      expect(resourceService.getAllGroups.calledWith(1, 10, ['id'])).to.be.true;
    });
  });

  describe('createPaginationConfig', () => {
    it('should create pagination config', () => {
      let expectedConfig = {
        showFirstLastButtons: true,
        page: 1,
        pageSize: 10,
        pageRotationStep: 2
      };

      resourceManagement.createPaginationConfig();

      expect(resourceManagement.paginationConfig).to.deep.equal(expectedConfig);
    });
  });

  describe('navigateToPageOfCreatedResource', () => {
    it('should change page if there is id of created resource', () => {
      resourceManagement.lastCreatedId = 'emf:user';
      resourceManagement.paginationConfig = {
        page: 1
      };

      resourceManagement.navigateToPageOfCreatedResource({pageNumber: 3});

      expect(resourceManagement.lastCreatedId).to.not.exist;
      expect(resourceManagement.paginationConfig.page).to.equal(3);
    });

    it('should do nothing when there is no id of created resource', () => {
      resourceManagement.paginationConfig = {
        page: 1
      };

      resourceManagement.navigateToPageOfCreatedResource({pageNumber: 3});

      expect(resourceManagement.paginationConfig.page).to.equal(1);
    });
  });

  describe('loadModels', () => {
    it('should invoke load models', () => {
      let userId = 'john';
      resourceManagement.loadModels(userId);

      expect(instanceService.loadModels.calledOnce).to.be.true;
      expect(instanceService.loadModels.getCall(0).args[0]).to.deep.equal([userId]);
    });
  });

  describe('batchLoad', () => {
    it('should invoke load batch deleted resources', () => {
      let usersIds = ['john', 'joe'];
      resourceManagement.batchLoad(usersIds);

      expect(instanceService.loadBatchDeleted.calledOnce).to.be.true;
      expect(instanceService.loadBatchDeleted.getCall(0).args[0]).to.deep.equal(usersIds);
    });
  });

  describe('createActionsConfig', () => {
    it('should create context property with configs', () => {
      resourceManagement.models = {definitionId: 'user'};
      resourceManagement.resources = [{id: 'reguaruser'}];

      resourceManagement.createActionsConfig();

      let context = resourceManagement.resources[0].context;
      expect(context).to.exist;
      expect(context.renderMenu).to.exist;
      expect(context.renderMenu()).to.be.true;
      expect(context.currentObject).to.be.an.instanceof(InstanceObject);
    });
  });

  describe('updatePaginationConfig', () => {
    it('should update total size', () => {
      resourceManagement.paginationConfig = {};
      resourceManagement.updatePaginationConfig(15);

      expect(resourceManagement.paginationConfig.total).to.equal(15);
    });
  });

  describe('createTableConfig', () => {
    it('should create proper table config', () => {
      let models = {
        definitionId: 'userDefinition'
      };
      resourceManagement.models = models;

      resourceManagement.createTableConfig();

      expect(resourceManagement.tableConfig).to.exist;
      expect(resourceManagement.tableConfig.models).to.exist;
      expect(resourceManagement.tableConfig.headerTitle).to.equal('user.management.table.header');
      expect(resourceManagement.tableConfig.definitionId).to.equal(models.definitionId);
      expect(resourceManagement.tableConfig.propertiesConfigKey).to.equal(Configuration.USER_MANAGEMENT_USER_PROPERTIES);
      expect(resourceManagement.tableConfig.editCallback).to.be.a('function');
      expect(resourceManagement.tableConfig.actionExecutedCallback).to.be.a('function');
    });
  });

  describe('reloadTable', () => {
    it('should reload resources', () => {
      resourceManagement.paginationConfig = {page: 2};

      resourceManagement.reloadTable();

      expect(resourceService.getAllUsers.calledOnce).to.be.true;
    });
  });

  describe('editResource', () => {
    it('should fetch resource data before edit', () => {
      let user = {
        id: 'emf:user'
      };
      resourceManagement.paginationConfig = {page: 1};
      resourceManagement.models = {};

      resourceManagement.editResource(user);

      expect(instanceService.load.calledWith(user.id, {params: {deleted: true}})).to.be.true;
    });
  });

  describe('createResource', () => {

    beforeEach(() => {
      resourceManagement.resources = [{
        properties: {
          'rdf:type': {
            results: ['emf:User']
          }
        }
      }];
      resourceManagement.models = {
        definitionId: 'userDefinition'
      };
    });

    it('should open preconfigured create dialog', () => {
      let expectedArgs = {
        contextSelectorDisabled: true,
        parentId: null,
        instanceData: null,
        instanceType: 'http://emf:User',
        instanceSubType: resourceManagement.models.definitionId,
        purpose: [ModelsService.PURPOSE_CREATE],
        controls: {
          showCreateMore: true
        },
        forceCreate: true,
        header: 'user.management.create',
        exclusions: ['file-upload-panel'],
        showTemplateSelector: true
      };

      resourceManagement.createResource();

      let actualArgs = createPanelService.openCreateInstanceDialog.getCall(0).args[0];
      expect(actualArgs.instanceCreatedCallback).to.exist;
      expect(actualArgs.onClosed).to.exist;

      delete actualArgs.instanceCreatedCallback;
      delete actualArgs.onClosed;

      expect(actualArgs).to.deep.equal(expectedArgs);
    });

    it('should record id of last created resource when callback is called', () => {
      resourceManagement.createResource();

      let args = createPanelService.openCreateInstanceDialog.getCall(0).args[0];
      args.instanceCreatedCallback({id: 'emf:user'});

      expect(resourceManagement.lastCreatedId).to.equal('emf:user');
    });

    it('should reload table on closed modal when a resource was created', () => {
      resourceManagement.paginationConfig = {
        page: 1
      };

      resourceManagement.createResource();

      let args = createPanelService.openCreateInstanceDialog.getCall(0).args[0];
      args.onClosed({instanceCreated: true});

      expect(resourceService.getAllUsers.calledOnce).to.be.true;
    });
  });

  function stubResourceService() {
    let resourceService = stub(ResourceRestService);
    resourceService.getAllUsers.returns(PromiseStub.resolve({
      data: {
        values: [{
          id: 'user'
        }],
        resultSize: 1
      }
    }));
    return resourceService;
  }

  function stubInstanceService() {
    let instanceService = stub(InstanceRestService);
    instanceService.load.returns(PromiseStub.resolve({
      data: {
        id: 'emf:user'
      }
    }));
    instanceService.loadBatchDeleted.returns(PromiseStub.resolve({
      data: []
    }));
    instanceService.loadModels.returns(PromiseStub.resolve({
      data: {
        user: {
          viewModel: {
            fields: []
          }
        }
      }
    }));
    instanceService.update.returns(PromiseStub.resolve({
      data: {
        'john': {
          id: 'john',
          properties: {
            'email': 'john@mail.com'
          }
        }
      }
    }));
    return instanceService;
  }

  function stubSaveDialogService() {
    let saveDialogService = stub(SaveDialogService);
    saveDialogService.openDialog.returns(PromiseStub.resolve({}));
    return saveDialogService;
  }

  function stubNamespaceService() {
    let namespaceService = stub(NamespaceService);
    namespaceService.toFullURI.returns(PromiseStub.resolve({
      data: {
        'emf:User': 'http://emf:User'
      }
    }));
    return namespaceService;
  }

});