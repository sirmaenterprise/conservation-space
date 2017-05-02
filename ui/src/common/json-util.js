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
}
