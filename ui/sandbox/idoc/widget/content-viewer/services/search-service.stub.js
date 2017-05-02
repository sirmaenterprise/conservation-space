import { Injectable, Inject } from 'app/app';
import { PromiseAdapter } from 'adapters/angular/promise-adapter';
import _ from 'lodash';
import data from 'sandbox/idoc/widget/content-viewer/services/search-service.data.json!';

const DEFAULT_DATASET = 'multiple';

@Injectable()
@Inject(PromiseAdapter)
export class SearchService {
  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
    this.dataset = DEFAULT_DATASET;
  }

  search() {
    return {
      promise: this.promiseAdapter.promise((resolve) => {
        // Cloning to prevent unexpected behaviour in the sandbox.
        resolve(_.cloneDeep(data[this.dataset]));
      }),
      timeout: this.promiseAdapter.resolve()
    }
  }
}