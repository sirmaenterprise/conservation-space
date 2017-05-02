import {Inject, Injectable} from 'app/app';
import {RestClient} from 'services/rest-client';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {SearchService, SEARCH_URL} from 'services/rest/search-service';
import {SearchResolverService} from 'services/resolver/search-resolver-service';

const EXTERNAL_SEARCH_URL = SEARCH_URL + '/external';

@Injectable()
@Inject(RestClient, SearchResolverService, PromiseAdapter)
export class ExternalSearchService extends SearchService {

  constructor(restClient, searchResolverService, promiseAdapter) {
    super(restClient, searchResolverService, promiseAdapter);
  }

  getSystemConfiguration(system) {
    return this.getConfiguration({context: system});
  }

  /**
   * This overrides the service URL in {@link SearchService} when performing POST requests.
   */
  getServiceUrl() {
    return EXTERNAL_SEARCH_URL;
  }
}