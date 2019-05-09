import {Inject, Injectable, NgTimeout} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import data from 'sandbox/administration/model-management/model-management-rest-service.data.json!';

@Injectable()
@Inject(NgTimeout, PromiseAdapter)
export class ModelManagementRestService {

  constructor($timeout, promiseAdapter) {
    this.$timeout = $timeout;
    this.promiseAdapter = promiseAdapter;

    this.saveStatus = ModelManagementRestService.SUCCESS_SAVE_STATUS;
    this.publishStatus = ModelManagementRestService.SUCCESS_PUBLISH_STATUS;
  }

  getModelsHierarchy() {
    return this.promiseAdapter.resolve(data.modelHierarchy);
  }

  getModelsMetaData() {
    return this.promiseAdapter.resolve(data.metaData);
  }

  getModelProperties() {
    return this.promiseAdapter.resolve(data.modelProperties);
  }

  getModelsForDeploy() {
    return this.promiseAdapter.resolve(ModelManagementRestService.PUBLISH_MAPPER[this.publishStatus]);
  }

  getModelData() {
    return this.promiseAdapter.promise((resolve) => {
      // small timeout to simulate actual response delay
      this.$timeout(() => resolve(data.modelData), 500);
    });
  }

  saveModelData() {
    return this.promiseAdapter.promise((resolve, reject) => {
      // small timeout to simulate actual response delay
      this.$timeout(() => ModelManagementRestService.SAVE_MAPPER[this.saveStatus](resolve, reject), 500);
    });
  }

  deployModels() {
    return this.promiseAdapter.promise((resolve) => {
      // small timeout to simulate actual response delay
      this.$timeout(() => resolve({}), 500);
    });
  }

  setSaveStatus(key) {
    this.saveStatus = key;
  }

  setPublishStatus(key) {
    this.publishStatus = key;
  }
}

ModelManagementRestService.FAIL_SAVE_STATUS = 'FAIL_SAVE';
ModelManagementRestService.SUCCESS_SAVE_STATUS = 'SUCCESS_SAVE';
ModelManagementRestService.FAIL_SAVE_STATUS_CONFLICT = 'FAIL_SAVE_CONFLICT';

ModelManagementRestService.FAIL_PUBLISH_STATUS = 'FAIL_PUBLISH';
ModelManagementRestService.SUCCESS_PUBLISH_STATUS = 'SUCCESS_PUBLISH';

ModelManagementRestService.PUBLISH_MAPPER = {};
ModelManagementRestService.PUBLISH_MAPPER[ModelManagementRestService.FAIL_PUBLISH_STATUS] = data.invalidModelsForDeployment;
ModelManagementRestService.PUBLISH_MAPPER[ModelManagementRestService.SUCCESS_PUBLISH_STATUS] = data.modelsForDeployment;

ModelManagementRestService.SAVE_MAPPER = {};
ModelManagementRestService.SAVE_MAPPER[ModelManagementRestService.SUCCESS_SAVE_STATUS] = (resolve, reject) => resolve({});
ModelManagementRestService.SAVE_MAPPER[ModelManagementRestService.FAIL_SAVE_STATUS_CONFLICT] = (resolve, reject) => reject(data.conflictOnSaveResponse);
ModelManagementRestService.SAVE_MAPPER[ModelManagementRestService.FAIL_SAVE_STATUS] = (resolve, reject) => reject(data.invalidModelsForSave);