import _ from 'lodash';
import {CURRENT_OBJECT_TEMP_ID} from 'models/instance-object';

/**
 * Utility class for common logic related to instance objects.
 *
 * @author A. Kunchev
 */
export class InstanceUtils {

  /**
   * Checks if given instance is a version. Supports raw and wrapped instance object.
   *
   * @param instance the instance that will be checked
   * @returns boolean - TRUE if the passed object has function #isVersion and it result is true, or if the passed object contains
   *          property `isVersion` and its value is true
   */
  static isVersion(instance) {
    if (!instance) {
      return false;
    }

    if (_.isFunction(instance.isVersion)) {
      return instance.isVersion();
    }

    return !!instance.properties && !!instance.properties.isVersion;
  }

  /**
   * Checks if the given id is equal to the temp id.
   *
   * @param id the id we want to check.
   *
   * @returns {boolean} true if id matches CURRENT_OBJECT_TEMP_ID, false otherwise.
   */
  static isTempId(id) {
    return id === CURRENT_OBJECT_TEMP_ID;
  }
}
