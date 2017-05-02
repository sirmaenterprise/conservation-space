import {Inject, Injectable} from 'app/app';
import {RestClient} from 'services/rest-client';
import data from 'sandbox/components/object-browser/object-browser-data.json!';

@Injectable()
@Inject(RestClient)
export class ObjectBrowserRestService {

  constructor(restClient) {
    this.restClient = restClient;
  }

  getChildNodes(id, params) {
    var result = data[id];

    if (!result) {
      result = data[params.rootId];
    }

    if (!result) {
      result = []
    }
    return Promise.resolve(result);
  }
}
