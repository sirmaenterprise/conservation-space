import {Injectable, Inject} from 'app/app';
import {InstanceRestService} from 'services/rest/instance-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

import data from 'sandbox/services/rest/headers-service.data.json!';

@Injectable()
@Inject(PromiseAdapter)
export class HeadersService {

  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
  }

  loadHeaders(ids, headerType, headers) {
    var result = headers || {};
    ids.forEach((id) => {
      result[id] = data[id];
    });
    return this.promiseAdapter.resolve(result);
  }
}