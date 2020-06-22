import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {HashGenerator} from 'common/hash-generator';
import _ from 'lodash';

const RELATIONS_URL = '/relations';

@Injectable()
@Inject(RestClient, PromiseAdapter)
export class RelationsService {

  constructor(client, promiseAdapter) {
    this.restClient = client;
    this.lisoMap = new Map();
    this.promiseAdapter = promiseAdapter;

    this.config = {
      headers: {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      }
    };
  }

  suggest(definitionId, propertyName, keywords, properties) {
    let timeout = this.promiseAdapter.defer();
    this.config.params = {properties};
    this.config.timeout = timeout.promise;
    this.config.skipInterceptor = true;


    return this.lastInSingleOut(
      RELATIONS_URL + '/suggest',
      [
        definitionId,
        propertyName,
        this.config
      ],
      () => {
        return this.restClient.post(RELATIONS_URL + '/suggest', {definitionId, propertyName, keywords}, this.config);
      },
      true,
      timeout
    );
  };

  /**
   * Hashes the passed parameters and uses them as a key in the class map. The value used is the request defer promise function.
   * Adds the calling REST function as unique string to the parameters before hashing to ensure proper work.
   * All requests to the same endpoint with same parameters (eq definitionId, propertyName and config) terminate the previous
   * back-end REST call.
   * It clones the results before return in order to serve different objects to the different callers.
   *
   * @param url to be used as the hash salt to ensure caller method uniqueness
   * @param params to identify equal calls
   * @param requestFunctionExecutor REST call function
   * @param extendedMode true to handle nested objects with duplicates. When not provided defaults to true
   * @param deferFunction defer function used to cancel oldder suggest calls
   *
   * returns cashed promise
   */
  lastInSingleOut(url, params, requestFunctionExecutor, extendedMode, deferFunction) {
    let hashKey = HashGenerator.getHash(params, extendedMode, url);

    let oldSuggestDefer = this.lisoMap.get(hashKey);
    oldSuggestDefer && oldSuggestDefer.resolve();

    this.lisoMap.set(hashKey, deferFunction);

    return requestFunctionExecutor().then((result) => {
      this.lisoMap.delete(hashKey);
      return _.clone(result, true);
    })
      .catch(() => {
        return this.promiseAdapter.resolve();
      });
  }
}
