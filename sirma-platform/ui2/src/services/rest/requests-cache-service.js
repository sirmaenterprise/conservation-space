import {Injectable} from 'app/app';
import _ from 'lodash';

/**
 * Provides functionality to reduce the REST calls made to the server.
 */
@Injectable()
export class RequestsCacheService {

  /**
   * Hashes the passed parameters and uses them as a key in a provided map. The value used is the request made.
   * Can hash arrays, objects, nested objects or primitives with random order. Adds the calling REST function as
   * unique string to the parameters before hashing to ensure proper work.
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
    let hashKey = this.getHash(params, extendedMode, url + requestFunctionExecutor);

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

  // Object hash value getter.
  // hashSalt is unique string value for each object source.
  getHash(object, extendedMode, hashSalt) {
    let string = this.convertToString(object, extendedMode) + hashSalt;
    return this.hash(string);
  }

  /**
   * Returns actual hash value in base 16 as string
   * @param string to hash
   * @returns {string} hash value
   */
  hash(string) {
    let hash = 0;
    for (let i = 0; i < string.length; i++) {
      hash = ((hash << 5) - hash) + string.charCodeAt(i);
      hash = hash | 0;
    }
    return hash.toString(16);
  }

  convertToString(object, extendedMode) {
    return _.isObject(object) ? '@' + this.objectToString(object, extendedMode) : this.valueToString(object);
  }

  /**
   * Converts value to string adding object type to the string.
   * @param value
   * @returns {*}
   */
  valueToString(value) {
    // used Object.prototype.toString.call(value) instead typeof as returns more info about the object
    // should not call value.toString() in order to handle null an undefined values
    return (Object.prototype.toString.call(value)) + value;
  }

  /**
   * Flattens objects as strings with object type and value
   * @param object
   * @param extendedMode if true removes duplicates, and allows array and object equality
   * @returns {string}
   */
  objectToString(object, extendedMode) {
    let converted = [];

    for (let key of Object.keys(object)) {
      if (object instanceof Array) {
        converted.push(this.convertToString(object[key], extendedMode));
      } else {
        converted.push(this.convertToString(key) + this.convertToString(object[key], extendedMode));
      }
    }

    if (extendedMode) {
      converted = converted.filter(function (item, index) {
        return converted.indexOf(item) === index;
      }).sort();
    }
    return (extendedMode ? '' : Object.prototype.toString.call(object)) + converted;
  }
}