import {Injectable, Inject} from 'app/app';
import {InstanceObject} from 'models/instance-object';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {InstanceRestService} from 'services/rest/instance-service';
import contextPath from 'sandbox/services/idoc/idoc-context-factory.data.json!';

@Injectable()
@Inject(PromiseAdapter, InstanceRestService)
export class IdocContextFactory {

  constructor(promiseAdapter, instanceRestService) {
    this.promiseAdapter = promiseAdapter;
    this.instanceRestService = instanceRestService;
  }

  getCurrentContext(currentObjectId, operation) {
    if(currentObjectId) {
      let instanceObject = this.instanceRestService.loadInstanceObject(currentObjectId, operation);
      return {
        getCurrentObject: () => {
          return Promise.resolve(instanceObject);
        }
      };
    }

    return {
      getCurrentObject: () => {
        let instance = new InstanceObject(contextPath[0].id);
        instance.setContextPath(contextPath);
        return Promise.resolve(instance);
      }
    };
  }

}