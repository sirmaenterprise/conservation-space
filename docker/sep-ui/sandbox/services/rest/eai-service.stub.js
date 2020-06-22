import {Injectable, Inject, NgTimeout} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import eaiData from 'sandbox/services/rest/eai-service.data.json!';
import config from 'sandbox/services/rest/services.config.json!';

@Injectable()
@Inject(PromiseAdapter, NgTimeout)
export class EAIService {
  constructor(promiseAdapter, $timeout) {
    this.promiseAdapter = promiseAdapter;
    this.$timeout = $timeout;
    this.config = config['eai'];
  }

  getModels(system) {
    return this.getTimeoutPromise({data: eaiData.models[system]});
  }

  getProperties(system, type) {
    return this.getTimeoutPromise({data: eaiData.properties[`${system}:${type}`]});
  }

  getRegisteredSystems() {
    return this.getTimeoutPromise({data: eaiData.systems});
  }

  getTimeoutPromise(resolveData) {
    return this.promiseAdapter.promise((resolve) => {
      this.$timeout(() => {
        resolve(resolveData);
      }, this.config.timeout);
    });
  }
}