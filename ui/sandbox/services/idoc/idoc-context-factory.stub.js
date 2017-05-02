import {Injectable, Inject} from 'app/app';
import {InstanceObject} from 'idoc/idoc-context';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import contextPath from 'sandbox/services/idoc/idoc-context-factory.data.json!';

@Injectable()
@Inject(PromiseAdapter)
export class IdocContextFactory {

  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
  }

  getCurrentContext() {
    return {
      getCurrentObject: () => {
        let instance = new InstanceObject(contextPath[0].id);
        instance.setContextPath(contextPath);
        return Promise.resolve(instance);
      }
    };
  }
}