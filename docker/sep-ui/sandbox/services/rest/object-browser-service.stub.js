import {Inject, Injectable} from 'app/app';
import {RestClient} from 'services/rest-client';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import data from 'sandbox/services/rest/object-browser-service.data.json!';

@Injectable()
@Inject(RestClient, PromiseAdapter)
export class ObjectBrowserRestService {

  constructor(restClient, promiseAdapter) {
    this.restClient = restClient;
    this.promiseAdapter = promiseAdapter;
  }

  getChildNodes(id, params) {
    var result = data[id];

    if (!result) {
      result = data[params.rootId];
    }

    if (!result) {
      result = []
    }
    return this.promiseAdapter.resolve(result);
  }
}