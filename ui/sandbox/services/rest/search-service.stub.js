import {Injectable, Inject, NgTimeout} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import data from 'sandbox/services/rest/search-service.data.json!';
import aggregatedData from 'sandbox/services/rest/search-service-aggregated.data.json!';
import config from 'sandbox/services/rest/services.config.json!';
import _ from 'lodash';

@Injectable()
@Inject(PromiseAdapter, NgTimeout)
export class SearchService {
  constructor(promiseAdapter, $timeout) {
    this.promiseAdapter = promiseAdapter;
    this.$timeout = $timeout;
    this.config = config['search'];
  }

  search(request) {
    let searchResults;
    if (request.arguments.groupBy) {
      searchResults = _.cloneDeep(aggregatedData);
    } else {
      searchResults = _.cloneDeep(data);
      if (request && request.arguments.pageSize && request.arguments.pageSize !== '0') {
        searchResults.data.values = data.data.values.slice(request.arguments.pageSize * (request.arguments.pageNumber - 1), request.arguments.pageSize * request.arguments.pageNumber);
      }
    }

    return {
      promise: this.promiseAdapter.promise((resolve) => {
        this.$timeout(() => {
          resolve(searchResults);
        }, this.config.timeout);
      }),
      timeout: this.promiseAdapter.resolve()
    };
  }
}