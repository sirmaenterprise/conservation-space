import {Injectable, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import _ from 'lodash';
import data from 'sandbox/idoc/widget/object-data-widget/data/search-service.data.json!';
import config from 'sandbox/services/rest/services.config.json!';

@Injectable()
@Inject(PromiseAdapter, '$timeout')
export class SearchService {
  constructor(promiseAdapter, $timeout) {
    this.promiseAdapter = promiseAdapter;
    this.$timeout = $timeout;
    this.config = config['search'];
    this.dataset = 'single';
  }

  search(request) {
    let searchResults = _.cloneDeep(data[this.dataset]);
    if (request && request.arguments.pageSize && request.arguments.pageSize !== '0') {
      searchResults.data.values = data.data.values.slice(request.arguments.pageSize * (request.arguments.pageNumber - 1), request.arguments.pageSize * request.arguments.pageNumber);
    }

    return {
      promise: this.promiseAdapter.promise((resolve) => {
        this.$timeout(() => {
          resolve(searchResults);
        }, this.config.timeout);
      }),
      timeout: this.promiseAdapter.resolve()
    }
  }
}