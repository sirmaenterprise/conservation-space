import {Injectable, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import _ from 'lodash';
import data from 'sandbox/idoc/widget/object-link-widget/services/instance-rest-service.data.json!';

const INSTANCE_DATA = "data";

@Injectable()
@Inject(PromiseAdapter)
export class InstanceRestService {

  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
  }

  load() {
    return this.promiseAdapter.promise((resolve) => {
      resolve({data: _.cloneDeep(data[INSTANCE_DATA])});
    });
  }
}