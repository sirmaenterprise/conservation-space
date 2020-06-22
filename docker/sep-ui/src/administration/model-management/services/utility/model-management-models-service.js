import {Inject, Injectable} from 'app/app';
import {ModelsService} from 'services/rest/models-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

import {ModelBase} from 'administration/model-management/model/model-base';
import {ModelManagementService} from 'administration/model-management/services/model-management-service';
import {ModelValidationDialogService} from 'administration/model-management/services/utility/model-validation-dialog-service';
import {ModelManagementLanguageService} from 'administration/model-management/services/utility/model-management-language-service';

import _ from 'lodash';

const ALL_TYPES = 'ALL_TYPES';

/**
 * Utility service providing means of collecting data about models. Service provides a indirect
 * means of extracting data about models. This model data is internally cached and can be shared
 * between multiple instances of the model management. This service is a direct wrapper for multiple
 * management services most important of which is the {@link ModelManagementService} which provides
 * the core implementation for accessing and converting response data to the API of the model management
 * This service ... the {@link ModelManagementService} by providing means of caching the processed model
 * data. Caching such things as meta data, models, data types, the main model hierarchy.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ModelsService, PromiseAdapter, ModelManagementService, ModelValidationDialogService, ModelManagementLanguageService)
export class ModelManagementModelsService {

  constructor(modelsService, promiseAdapter, modelManagementService, modelValidationDialogService, modelManagementLanguageService) {
    this.initializeTypesCache();

    this.modelsService = modelsService;
    this.promiseAdapter = promiseAdapter;

    this.modelManagementService = modelManagementService;
    this.modelValidationDialogService = modelValidationDialogService;
    this.modelManagementLanguageService = modelManagementLanguageService;
  }

  initializeTypesCache() {
    this.types = {};
  }

  getTypes(type) {
    type = type || ALL_TYPES;
    if (!this.types[type]) {
      return this.modelsService.getTypes(type).then(types => {
        this.cacheTypes(type, types);
        return this.getCached(type);
      });
    }
    return this.promiseAdapter.resolve(this.getCached(type));
  }

  getModel(id) {
    if (!this.models || !this.meta) {
      return this.getModels()
        .then(() => this.modelManagementService.getModel(id, this.models, this.meta))
        .then((response) => this.setModelVersion(response.getVersion()) && response);
    }
    return this.modelManagementService.getModel(id, this.models, this.meta)
      .then((response) => this.setModelVersion(response.getVersion()) && response);
  }

  getModels(reload) {
    if (!this.models || reload) {
      return this.promiseAdapter.all([
        this.getMetaData(reload), this.getHierarchy(reload)
      ]).then(([metaData]) => {
        return this.modelManagementService.getProperties(metaData);
      }).then((properties) => {
        this.models = this.modelManagementService.getModelStore(this.hierarchy, properties);
        return this.models;
      });
    }
    return this.promiseAdapter.resolve(this.models);
  }

  getDeploymentModels() {
    if (!this.models || !this.meta) {
      return this.getModels().then(() => this.modelManagementService.getModelsForDeploy(this.models));
    }
    return this.modelManagementService.getModelsForDeploy(this.models);
  }

  getMetaData(reload) {
    if (!this.meta || reload) {
      return this.modelManagementService.getMetaData().then(meta => {
        this.meta = meta;
        return meta;
      });
    }
    return this.promiseAdapter.resolve(this.meta);
  }

  getHierarchy(reload) {
    if (!this.hierarchy || reload) {
      return this.modelManagementService.getHierarchy().then(hierarchy => {
        this.hierarchy = hierarchy;
        return hierarchy;
      });
    }
    return this.promiseAdapter.resolve(this.hierarchy);
  }

  save(changes) {
    return this.modelManagementService.saveModels(changes, this.modelVersion).then((response) => {
      // on successful save get the new model version
      this.setModelVersion(response.modelVersion);
    }).catch(reject => {
      // on failed save show a proper dialog to the client
      this.modelValidationDialogService.create(reject, this.models);
      return this.promiseAdapter.reject(reject);
    });
  }

  deploy(deploymentRequest) {
    return this.modelManagementService.deployModels(deploymentRequest);
  }

  getCached(type) {
    let types = this.types[type];
    return types.map(t => this.getType(t));
  }

  getType(type) {
    return {id: type.id, label: this.getDescription(type)};
  }

  getDescription(type) {
    return this.modelManagementLanguageService.getApplicableDescription(lang => type.labels[lang]);
  }

  addModel(model) {
    if (model instanceof ModelBase) {
      this.models.addModel(model);
    }
    return this;
  }

  hasModel(id, type) {
    return !!this.models.getModel(id, type);
  }

  cacheTypes(type, types) {
    this.types[type] = types;
  }

  setModelVersion(version) {
    if (_.isUndefined(this.modelVersion) || version > this.modelVersion) {
      this.modelVersion = version;
    }
    return this;
  }
}