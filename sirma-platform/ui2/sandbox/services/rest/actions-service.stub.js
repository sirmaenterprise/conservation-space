import {Injectable, Inject} from 'app/app';
import {RestClient} from 'services/rest-client';
import { PromiseAdapter } from 'adapters/angular/promise-adapter';
import {InstanceResponse} from 'services/rest/response/instance-response';
import actions from 'sandbox/services/rest/actions-service.data.json!';
import instanceData from 'sandbox/services/rest/instance-service.data.json!';

@Injectable()
@Inject(RestClient, PromiseAdapter)
export class ActionsService {

  constructor(restClient, promiseAdapter) {
    this.restClient = restClient;
    this.promiseAdapter = promiseAdapter;
  }

  lock(id, data) {
    this.restClient.get('/');
    return new Promise((resolve) => {
      resolve({});
    });
  };

  unlock(id, data) {
    this.restClient.get('/');
    return new Promise((resolve) => {
      resolve({});
    });
  };

  createOrUpdate(id, data) {
    return this.promiseAdapter.promise((resolve) => {
      data.id = id;

      if (!data.headers) {
        data.headers = {
          breadcrumb_header: '<div>id</div>'
        };
      }

      sessionStorage.setItem(id, JSON.stringify(data));
      resolve({data: data});
    });
  };

  getChangeTypeInstance(id, asType) {
    let idoc = this.getStubbedObject(id);
    return this.promiseAdapter.resolve(new InstanceResponse({data: idoc}));
  }

  changeType(id, data) {
    let idoc = this.getStubbedObject(id);
    return this.promiseAdapter.resolve(new InstanceResponse({data: idoc}));
  }

  getActions(id, data) {
    // this.restClient.get('/');
    return this.promiseAdapter.promise((resolve) => {
      resolve({
        data: actions
      });
    });
  }

  getFlatActions(id, data) {
    // this.restClient.get('/');
    return this.promiseAdapter.promise((resolve) => {
      resolve({
        data: actions
      });
    });
  }

  addIcons(id, icons) {
    return this.promiseAdapter.resolve();
  }

  executeTransition() {
    return this.promiseAdapter.resolve({data: {}});
  }

  getStubbedObject(id) {
    let object = JSON.parse(sessionStorage.getItem(id));

    if (!object) {
      object = _.cloneDeep(instanceData.instances[id]);
    }

    return object;
  }

}