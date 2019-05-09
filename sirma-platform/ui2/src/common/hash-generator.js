import _ from 'lodash';

/**
 * Can hash arrays, objects, nested objects or primitives with random order.
 * In extended mode handles nested objects with duplicates.
 */
export class HashGenerator {

  /**
   * Object hash value getter.
   *
   * @param object to create hash from. Can be array, object, nested object or primitives with random order.
   * @param extendedMode if true - handles nested objects with duplicates
   * @param hashSalt unique string value for each object source
   * @returns {string} generated hash
   */
  static getHash(object, extendedMode, hashSalt) {
    let string = HashGenerator._convertToString(object, extendedMode) + hashSalt;
    return HashGenerator._hash(string);
  }

  /**
   * Returns actual hash value in base 16 as string
   * @param string to hash
   * @returns {string} hash value
   */
  static _hash(string) {
    let hash = 0;
    for (let i = 0; i < string.length; i++) {
      hash = ((hash << 5) - hash) + string.charCodeAt(i);
      hash = hash | 0;
    }
    return hash.toString(16);
  }

  static _convertToString(object, extendedMode) {
    return _.isObject(object) ? '@' + HashGenerator._objectToString(object, extendedMode) : HashGenerator._valueToString(object);
  }

  /**
   * Converts value to string adding object type to the string.
   * @param value
   * @returns {*}
   */
  static _valueToString(value) {
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
  static _objectToString(object, extendedMode) {
    let converted = [];

    for (let key of Object.keys(object)) {
      if (object instanceof Array) {
        converted.push(HashGenerator._convertToString(object[key], extendedMode));
      } else {
        converted.push(HashGenerator._convertToString(key) + HashGenerator._convertToString(object[key], extendedMode));
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
