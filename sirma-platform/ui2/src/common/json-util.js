import base64 from 'common/lib/base64';
import _ from 'lodash';

export class JsonUtil {
  /**
   * Copy source[sourceKey] value into object[objectKey].
   *
   * @sourceKey - optional parameter. If missing objectKey will be used instead
   */
  static copyProperty(object, objectKey, source, sourceKey) {
    sourceKey = sourceKey || objectKey;
    if (object === undefined || objectKey === undefined || source === undefined || !source.hasOwnProperty(sourceKey)) {
      return;
    }
    object[objectKey] = source[sourceKey];
  }

  /**
   * Verifies if provided string can be safely converted to a valid json which to be used.
   *
   * It is important to take in account that this method doesn't guarantee the object returned from JSON.parse is
   * necessary a json object. It could be a number "5" for example or "true|false|null", and all these are valid json
   * values.
   * @see https://tools.ietf.org/html/rfc7159
   *
   * @param string
   * @returns {boolean}
   */
  static isJson(string) {
    try {
      JSON.parse(string);
    } catch (e) {
      return false;
    }
    return true;
  }

  /**
   * Removes a list of specified properties from a given object - in place
   * The method performs a deep removal of properties, meaning that nested
   * objects at any level will also be affected.
   *
   * @param object the object which properties are to be removed
   * @param blackList the list of properties to be removed
   */
  static removeObjectProperties(object, blackList) {
    for (let key in object) {
      // delete all properties specified inside the black list
      _.forEach(blackList, (toDelete) => object.hasOwnProperty(toDelete) && delete object[toDelete]);

      let property = object[key];
      if (property && _.isObject(property)) {
        // traverse down existing properties to sanitize
        JsonUtil.removeObjectProperties(property, blackList);
      }
    }
    return object;
  }

  /**
   * Encodes a given object to a base64 format
   *
   * @param object the object to be encoded
   */
  static encode(object) {
    if (_.isObject(object)) {
      return base64.encode(JSON.stringify(object));
    }
  }

  /**
   * Decodes a given object from a base64 format
   *
   * @param base64string the base64 string to be decoded
   */
  static decode(base64string = '') {
    let decoded = base64.decode(base64string);

    if (JsonUtil.isJson(decoded)) {
      return JSON.parse(decoded);
    }
  }
}
