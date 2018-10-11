import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {SearchResolverService} from 'services/resolver/search-resolver-service';
import {RequestsCacheService} from 'services/rest/requests-cache-service';
import _ from 'lodash';

export const SEARCH_URL = '/search';

@Injectable()
@Inject(RestClient, SearchResolverService, PromiseAdapter, RequestsCacheService)
export class SearchService {

  constructor(restClient, searchResolverService, promiseAdapter, requestsCacheService) {
    this.restClient = restClient;
    this.searchResolverService = searchResolverService;
    this.promiseAdapter = promiseAdapter;
    this.requestsMap = new Map();
    this.requestsCacheService = requestsCacheService;

    this.config = {
      headers: {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      }
    };
  }

  /**
   * Retrieves search configuration used for customizing the behaviour of the search based on models
   * defined in the system.
   *
   * @param params - map to be applied as query parameters to the request
   * @returns promise resolving the configuration
   */
  getConfiguration(params) {
    var config = _.defaultsDeep({params}, this.config);
    return this.restClient.get(SEARCH_URL + '/configuration', config);
  }

  /**
   * Performs a search in the system based on the provided search request. Example search request:
   *  {
   *    query: {...}, // instance of {@link QueryBuilder} containing the criteria tree
   *    arguments: {...}, // map of arguments to be applied as parameters to the http request
   *    context: {...} // the iDoc context instance used for resolving instances
   *  }
   *
   * Performs resolving on the search tree before sending the http request.
   *
   * @param searchRequest - the search request with criteria & arguments
   * @returns object containing the http promise with the results and a timeout function for stopping the request
   */
  search(searchRequest) {
    var request = searchRequest || {};
    var timeout = this.promiseAdapter.defer();

    var searchResult = this.resolveTree(request).then(() => {
      return this.searchInternal(request, timeout);
    });

    return {
      promise: searchResult,
      timeout: timeout
    };
  }

  resolveTree(searchRequest) {
    if (searchRequest && searchRequest.query && searchRequest.query.tree) {
      return this.searchResolverService.resolve(searchRequest.query.tree, searchRequest.context);
    }
    // No tree for resolving.
    return this.promiseAdapter.resolve();
  }

  searchInternal(request, timeout) {
    var options = this.getOptions(request, timeout);

    var data = {};
    if (request.query && request.query.tree) {
      // Sanitizing the tree - removing any angular specific $$ object keys
      data = angular.copy(request.query.tree);
    }
    return this.requestsCacheService.cache(this.getServiceUrl(), [data, options], this.requestsMap, () => {
      return this.restClient.post(this.getServiceUrl(), data, options);
    });
  }

  getOptions(request, timeout) {
    return _.defaultsDeep({
      params: request.arguments || {},
      timeout: timeout.promise,
      skipInterceptor: request.skipInterceptor || false
    }, this.config);
  }

  getServiceUrl() {
    return SEARCH_URL;
  }
}
