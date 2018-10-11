import {Injectable, Inject, NgTimeout} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import data from 'sandbox/administration/model-management/model-management-rest-service.data.json!';

@Injectable()
@Inject(NgTimeout, PromiseAdapter)
export class ModelManagementRestService {

  constructor($timeout, promiseAdapter) {
    this.$timeout = $timeout;
    this.promiseAdapter = promiseAdapter;
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

  getModelData() {
    return this.promiseAdapter.promise((resolve) => {
      // small timeout to simulate actual response delay
      this.$timeout(() => resolve(data.modelData), 100);
    });
  }
}