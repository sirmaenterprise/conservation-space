import {Injectable} from 'app/app';
import {HashGenerator} from 'common/hash-generator';
import _ from 'lodash';

/**
 * Provides functionality to reduce the REST calls made to the server.
 */
@Injectable()
export class RequestsCacheService {

  /**
   * Hashes the passed parameters and uses them as a key in a provided map. The value used is the request made.
   * Adds the calling REST function as unique string to the parameters before hashing to ensure proper work.
   * It clones the results before return in order to serve different objects to the different callers.
   *
   * @param url to be used as part of the hash salt to ensure caller method uniqueness
   * @param params to identify equal calls
   * @param requestsMap map object, holds all active requests
   * @param requestFunctionExecutor REST call function. Used as part of the hashSalt to ensure caller method uniqueness
   * @param extendedMode true to handle nested objects with duplicates. When not provided defaults to true
   *
   * returns cashed promise
   */
  cache(url, params, requestsMap, requestFunctionExecutor, extendedMode = true) {
    let hashKey = HashGenerator.getHash(params, extendedMode, url + requestFunctionExecutor);

    if (requestsMap.has(hashKey)) {
      return new Promise((resolve) => {
        return requestsMap.get(hashKey).then(result => resolve(_.clone(result, true)));
      }).catch((error) => {
        throw error;
      });
    } else {
      let promise = requestFunctionExecutor();
      requestsMap.set(hashKey, promise);
      return promise.then((result) => {
        requestsMap.delete(hashKey);
        return _.clone(result, true);
      })
        .catch((error) => {
          requestsMap.delete(hashKey);
          throw error;
        });
    }
  }
}