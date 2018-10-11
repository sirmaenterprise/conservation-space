import {Injectable, Inject} from 'app/app';
import {RestClient} from 'services/rest-client';
import { PromiseAdapter } from 'adapters/angular/promise-adapter';
import actions from 'sandbox/services/rest/actions-service.data.json!';

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

}