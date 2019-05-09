import {Inject, Injectable} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

import {ModelManagementRestService} from './model-management-rest-service';
import {ModelManagementCopyService} from './model-management-copy-service';

import {ModelList} from 'administration/model-management/model/model-list';
import {ModelOperation} from 'administration/model-management/model/model-operation';
import {ModelResponse} from 'administration/model-management/model/model-response';
import {ModelDeployRequest} from 'administration/model-management/model/request/model-deploy-request';
import {ModelAttributeTypes} from 'administration/model-management/model/attributes/model-attribute-types';
import {ModelValidationReport} from 'administration/model-management/model/validation/model-validation-report';

import {ModelStoreBuilder} from 'administration/model-management/services/builders/model-store-builder';
import {ModelDataBuilder} from 'administration/model-management/services/builders/model-data-builder';
import {ModelMetaDataBuilder} from 'administration/model-management/services/builders/model-meta-builder';
import {ModelPropertyBuilder} from 'administration/model-management/services/builders/model-property-builder';
import {ModelHierarchyBuilder} from 'administration/model-management/services/builders/model-hierarchy-builder';
import {ModelPathBuilder} from 'administration/model-management/services/builders/model-path-builder';

import _ from 'lodash';

/**
 * Administration service for managing models in the system.
 * Responsible for converting models into proper structure for the web application.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ModelManagementRestService, ModelManagementCopyService, PromiseAdapter, ModelDataBuilder, ModelHierarchyBuilder, ModelMetaDataBuilder, ModelPropertyBuilder, ModelStoreBuilder, ModelPathBuilder)
export class ModelManagementService {

  constructor(modelManagementRestService, modelManagementCopyService, promiseAdapter, modelDataBuilder, modelHierarchyBuilder, modelMetaDataBuilder, modelPropertyBuilder, modelStoreBuilder, modelPathBuilder) {
    this.promiseAdapter = promiseAdapter;
    this.modelManagementRestService = modelManagementRestService;
    this.modelManagementCopyService = modelManagementCopyService;

    this.modelDataBuilder = modelDataBuilder;
    this.modelPropertyBuilder = modelPropertyBuilder;
    this.modelHierarchyBuilder = modelHierarchyBuilder;
    this.modelMetaDataBuilder = modelMetaDataBuilder;
    this.modelStoreBuilder = modelStoreBuilder;
    this.modelPathBuilder = modelPathBuilder;
  }

  getModel(id, models, meta) {
    let model = models.getModel(id);
    let payload = new ModelResponse(model);
    let toLoad = this.findFirstNotLoadedModel(model);

    if (toLoad) {
      return this.modelManagementRestService.getModelData(toLoad.getId()).then(data => {
        // construct all models provided by the response data
        this.modelDataBuilder.buildModels(models, meta, data);
        // build the inheritance model for the given model
        this.modelDataBuilder.buildModelLinks(model, models);
        // set the current model version and return the payload
        return payload.setVersion(data.modelVersion) && payload;
      });
    }

    // build the inheritance model for the given model
    this.modelDataBuilder.buildModelLinks(model, models);
    // has nothing to load so simply return the response with the model
    return model ? this.promiseAdapter.resolve(payload) : this.promiseAdapter.reject();
  }

  getModelStore(hierarchy, properties) {
    let models = this.modelStoreBuilder.buildStoreFromHierarchy(hierarchy);
    return this.modelStoreBuilder.buildStoreFromProperties(properties, models);
  }

  getMetaData() {
    return this.modelManagementRestService.getModelsMetaData().then(meta => {
      // directly call the meta data builder which provides built data
      return this.modelMetaDataBuilder.buildMetaData(meta);
    });
  }

  getHierarchy() {
    return this.modelManagementRestService.getModelsHierarchy().then(hierarchy => {
      // directly call the hierarchy builder which provides built hierarchy
      return this.modelHierarchyBuilder.buildHierarchy(hierarchy);
    });
  }

  getProperties(meta) {
    return this.modelManagementRestService.getModelProperties().then(properties => {
      // directly call the property builder which provides the built properties
      return this.modelPropertyBuilder.buildProperties(properties, meta.getProperties());
    });
  }

  /**
   * Performs a request to the server to retrieve the nodes applicable for deployment.
   *
   * @param store models store used to map model IDs to their instances
   * @returns a deployment request instance of {@link ModelDeployRequest}
   */
  getModelsForDeploy(store) {
    return this.modelManagementRestService.getModelsForDeploy().then(deploymentReport => {
      let forDeployment = new ModelList();
      deploymentReport.nodes.forEach(model => {
        model = store.getModel(model.id);
        model && forDeployment.insert(model);
      });
      return new ModelDeployRequest()
        .setModels(forDeployment)
        .setVersion(deploymentReport.version)
        .setValidationReport(new ModelValidationReport(deploymentReport));
    });
  }

  saveModels(changes, modelVersion) {
    changes = changes.map(change => this.transformChangeSet(change));
    return this.modelManagementRestService.saveModelData({modelVersion, changes});
  }

  /**
   * Performs a request for deploying the models defined in the request.
   *
   * @param deploymentRequest instance of {@link ModelDeployRequest}
   * @returns {Promise} resolving when the server responds successfully
   */
  deployModels(deploymentRequest) {
    let modelIds = deploymentRequest.getSelectedModels().getModels().map(model => model.getId());
    let version = deploymentRequest.getVersion();
    // model deployment process requires with model identifiers
    return this.modelManagementRestService.deployModels(modelIds, version);
  }

  findFirstNotLoadedModel(model) {
    if (model) {
      let models = [model, ...model.getParents()];
      return _.find(models, model => !model.isLoaded());
    }
    return null;
  }

  transformChangeSet(change) {
    // Transforming the change set to a proper request data structure required by
    // the service API. This also prevents direct modification of the already
    // constructed change set instance which might be internally reused or needed.
    return {
      selector: change.getSelector(),
      newValue: this.getValue(change.getNewValue()),
      oldValue: this.getValue(change.getOldValue()),
      operation: this.getOperation(change.getOperation(), change.getModel())
    };
  }

  getValue(value) {
    //TODO: handle object values as well when properly supported
    let resolved = ModelManagementService.VALUE_RESOLVER[value];
    return !_.isUndefined(resolved) ? resolved : value;
  }

  getOperation(operation, model) {
    let resolved = ModelManagementService.OPERATION_RESOLVER[operation];
    return !_.isUndefined(resolved) ? resolved(model) : operation;
  }
}

ModelManagementService.VALUE_RESOLVER = {};
ModelManagementService.VALUE_RESOLVER[''] = null;

ModelManagementService.OPERATION_RESOLVER = {
  [ModelOperation.RESTORE]: () => 'restore',
  [ModelOperation.REMOVE]: () => 'remove',
  [ModelOperation.MODIFY]: model => ModelAttributeTypes.isMultiValued(model.getType()) ? 'modifyMapAttribute' : 'modifyAttribute'
};