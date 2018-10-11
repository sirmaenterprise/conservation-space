import {View, Component, Inject} from 'app/app';
import {Configurable} from 'components/configurable';
import {ResourceRestService} from 'services/rest/resources-service';
import {InstanceRestService, EDIT_OPERATION_NAME} from 'services/rest/instance-service';
import {SaveDialogService} from 'idoc/save-idoc-dialog/save-dialog-service';
import {CreatePanelService} from 'services/create/create-panel-service';
import {NamespaceService} from 'services/rest/namespace-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {Configuration} from 'common/application-config';
import {InstanceObject} from 'models/instance-object';
import {FormWrapper} from 'form-builder/form-wrapper';
import {ModelsService} from 'services/rest/models-service';
import {Logger} from 'services/logging/logger';
import {RDF_TYPE} from 'instance/instance-properties';
import 'administration/resources-management/resources-table/resources-table';
import _ from 'lodash';

import './resource-management.css!css';
import template from './resource-management.html!text';

export const USER = 'user';
export const GROUP = 'group';

/**
 * Administration component for user and group management.
 *
 * It uses the resources-table component for rendering table with the resources. Also uses the pagination component for
 * the pagination of the table.
 *
 * config: {
 *    resourceType: user or group, depending on this property the given resource management will be rendered,
 *    if explicitly not specified user management will be rendered by default
 * }
 */
@Component({
  selector: 'seip-resource-management',
  properties: {
    'config': 'config'
  }
})
@View({
  template
})
@Inject(ResourceRestService, InstanceRestService, SaveDialogService, CreatePanelService, NamespaceService, PromiseAdapter, Logger)
export class ResourceManagement extends Configurable {

  constructor(resourceService, instanceService, saveDialogService, createPanelService, namespaceService, promiseAdapter, logger) {
    super({
      resourceType: USER
    });

    this.resourceService = resourceService;
    this.instanceService = instanceService;
    this.saveDialogService = saveDialogService;
    this.createPanelService = createPanelService;
    this.namespaceService = namespaceService;
    this.promiseAdapter = promiseAdapter;
    this.logger = logger;
  }

  ngOnInit() {
    this.initConfig();

    this.createPaginationConfig();

    this.loadResources();
  }

  initConfig() {
    this.propertiesConfigKey = Configuration.USER_MANAGEMENT_USER_PROPERTIES;
    this.headerLabel = 'user.management.table.header';
    this.createLabel = 'user.management.create';
    this.resourceLoader = (...args) => {
      return this.resourceService.getAllUsers(...args);
    };

    if (this.config.resourceType === GROUP) {
      this.propertiesConfigKey = Configuration.GROUP_MANAGEMENT_GROUP_PROPERTIES;
      this.headerLabel = 'group.management.table.header';
      this.createLabel = 'group.management.create';
      this.resourceLoader = (...args) => {
        return this.resourceService.getAllGroups(...args);
      };
    }
  }

  createPaginationConfig() {
    this.paginationConfig = {
      showFirstLastButtons: true,
      page: ResourceManagement.FIRST_PAGE,
      pageSize: ResourceManagement.PAGE_SIZE,
      pageRotationStep: 2
    };

    this.paginationCallback = this.onPageChanged.bind(this);
  }

  onPageChanged(params) {
    this.loadResources(params.pageNumber);
  }

  loadResources(page) {
    let currentPage = page || ResourceManagement.FIRST_PAGE;

    // retrieve ids of all resources, including the ones that are deactivated (deleted)
    this.resourceLoader(currentPage, ResourceManagement.PAGE_SIZE, ['id'], this.lastCreatedId).then((response) => {
      this.navigateToPageOfCreatedResource(response.data);
      this.updatePaginationConfig(response.data.resultSize);

      return response.data.values.map(resource => resource.id);
    }).then((resourcesIds) => {
      return this.promiseAdapter.all([this.loadModels(resourcesIds[0]), this.batchLoad(resourcesIds)]);
    }).then((responses) => {
      let modelsData = responses[0].data;
      let resourceId = Object.keys(modelsData)[0];

      this.models = responses[0].data[resourceId];
      this.resources = responses[1].data;

      this.createActionsConfig();
      this.createTableConfig();
    }).catch(() => {
      this.logger.error('Error occurred while retrieving resources');
    });
  }

  navigateToPageOfCreatedResource(response) {
    if (this.lastCreatedId) {
      this.paginationConfig.page = response.pageNumber;
      delete this.lastCreatedId;
    }
  }

  loadModels(resourceId) {
    return this.instanceService.loadModels([resourceId], EDIT_OPERATION_NAME);
  }

  batchLoad(resourcesIds) {
    return this.instanceService.loadBatchDeleted(resourcesIds);
  }

  createActionsConfig() {
    this.resources.forEach((resource) => {
      resource.context = {
        currentObject: new InstanceObject(resource.id, _.cloneDeep(this.models)),
        renderMenu: () => true
      };
    });
  }

  updatePaginationConfig(resultSize) {
    this.paginationConfig.total = resultSize;
  }

  createTableConfig() {
    this.tableConfig = {
      headerTitle: this.headerLabel,
      propertiesConfigKey: this.propertiesConfigKey,
      models: _.cloneDeep(this.models),
      definitionId: this.models.definitionId,
      editCallback: this.editResource.bind(this),
      actionExecutedCallback: this.reloadTable.bind(this)
    };
  }

  editResource(resource) {
    let instanceObject;
    let resourceData;

    this.instanceService.load(resource.id, {params: {deleted: true}}).then((response) => {
      resourceData = response.data;

      instanceObject = new InstanceObject(resourceData.id, _.cloneDeep(this.models));
      instanceObject.mergePropertiesIntoModel(resourceData.properties);

      let resourcesToEdit = {};
      resourcesToEdit[resourceData.id] = this.buildFormConfig(resourceData, instanceObject);

      return this.saveDialogService.openDialog({
        formConfig: {
          formViewMode: FormWrapper.FORM_VIEW_MODE_EDIT,
          renderMandatory: false
        },
        models: resourcesToEdit
      });
    }).then(() => {
      return this.instanceService.update(instanceObject.getId(), {
        definitionId: resourceData.definitionId,
        properties: instanceObject.getChangeset(false)
      });
    }).then(() => {
      this.reloadTable();
    }).catch((error) => {
      if (error) {
        this.logger.error(error);
      }
    });
  }

  buildFormConfig(resource, instanceObject) {
    return {
      models: {
        id: resource.id,
        headers: resource.headers,
        instanceType: resource.instanceType,
        definitionId: resource.definitionId,
        definitionLabel: instanceObject.getModels().definitionLabel,
        validationModel: instanceObject.getModels().validationModel,
        viewModel: instanceObject.getModels().viewModel
      }
    };
  }

  reloadTable() {
    delete this.resources;
    delete this.models;
    this.loadResources(this.paginationConfig.page);
  }

  createResource() {
    let rdfType = this.resources[0].properties[RDF_TYPE].results[0];

    // create panel needs full uri to determine the instance type
    this.namespaceService.toFullURI([rdfType]).then((fullUris) => {
      this.createPanelService.openCreateInstanceDialog({
        contextSelectorDisabled: true,
        parentId: null,
        instanceData: null,
        instanceType: fullUris.data[rdfType],
        instanceSubType: this.models.definitionId,
        purpose: [ModelsService.PURPOSE_CREATE],
        controls: {
          showCreateMore: true
        },
        forceCreate: true,
        header: this.createLabel,
        exclusions: ['file-upload-panel'],
        showTemplateSelector: true,
        instanceCreatedCallback: (instance) => {
          // record last created id
          this.lastCreatedId = instance.id;
        },
        onClosed: (result) => {
          // if resource is really created reload table and navigate to the page where new resource is located
          if (result.instanceCreated) {
            this.reloadTable();
          }
        }
      });
    });
  }

}

ResourceManagement.PAGE_SIZE = 10;
ResourceManagement.FIRST_PAGE = 1;