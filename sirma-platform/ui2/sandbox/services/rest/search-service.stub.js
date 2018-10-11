import {Inject, Injectable, NgTimeout} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import _ from 'lodash';
import data from 'sandbox/services/rest/search-service.data.json!';
import aggregatedData from 'sandbox/services/rest/search-service-aggregated.data.json!';
import config from 'sandbox/services/rest/services.config.json!';

@Injectable()
@Inject(PromiseAdapter, NgTimeout)
export class SearchService {
  constructor(promiseAdapter, $timeout) {
    this.promiseAdapter = promiseAdapter;
    this.$timeout = $timeout;
    this.config = config['search'];
  }

  search(request) {
    let instanceIds = SearchCriteriaUtils.getFieldValuesFromCriteria(request.query.tree, 'instanceId');

    let searchResults;
    if (request.arguments.groupBy) {
      searchResults = _.cloneDeep(aggregatedData);
    } else {
      searchResults = _.cloneDeep(data);

      let values = data.data.values;
      if (instanceIds.length > 0) {
        values = values.filter((value) => {
          return instanceIds.indexOf(value.id) !== -1;
        });
      }

      if (request && request.arguments.pageSize && request.arguments.pageNumber) {
        values = values.slice(request.arguments.pageSize * (request.arguments.pageNumber - 1), request.arguments.pageSize * request.arguments.pageNumber);
      }

      searchResults.data.values = values;
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