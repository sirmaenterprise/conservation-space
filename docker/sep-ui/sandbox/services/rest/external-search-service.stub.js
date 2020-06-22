import {Injectable, Inject, NgTimeout} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {SearchService} from 'sandbox/services/rest/search-service.stub';
import config from 'sandbox/services/rest/services.config.json!';
import data from 'sandbox/services/rest/external-search-service.data.json!';

@Injectable()
@Inject(PromiseAdapter, NgTimeout)
export class ExternalSearchService extends SearchService {

  constructor(promiseAdapter, $timeout) {
    super(promiseAdapter, $timeout);
    this.config = config['external-search'];
  }

  getSystemConfiguration(system) {
    return this.promiseAdapter.promise((resolve) => {
      this.$timeout(() => {
        resolve({data: data.configuration[system]});
      }, this.config.timeout);
    });
  }
}
