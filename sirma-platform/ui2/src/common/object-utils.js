export function decorate(originalObject, decoratedMethods) {
  var result = {};

  getMethods(Object.getPrototypeOf(originalObject)).forEach(function (method) {
    result[method] = function () {
      return originalObject[method].apply(originalObject, arguments);
    };
  });

  Object.keys(decoratedMethods).forEach(function (method) {
    result[method] = decoratedMethods[method].bind(result);
  });

  // proxy the properties
  Object.keys(originalObject).forEach(function (property) {
    if (typeof originalObject[property] !== 'function') {
      Object.defineProperty(result, property, {
        set: function (value) {
          originalObject[property] = value;
        },
        get: function () {
          return originalObject[property];
        }
      });
    }
  });

  result.undecorate = function () {
    Object.keys(decoratedMethods).forEach(function (method) {
      result[method] = function () {
        return originalObject[method].apply(originalObject, arguments);
      };
    });
  };

  result.getWrappedObject = function() {
    return originalObject;
  };

  return result;
}

export function filterBy(obj, filterFunc) {
  if (!obj || typeof filterFunc !== 'function') {
    return obj;
  }
  let result = {};
  Object.keys(obj).forEach((objKey) => {
    if(filterFunc(obj[objKey], objKey)) {
      result[objKey] = obj[objKey];
    }
  });
  return result;
}

function getMethods(prototype) {
  var methods = new Set();

  Object.getOwnPropertyNames(prototype).forEach(function (property) {
    if (typeof prototype[property] === 'function') {
      methods.add(property);
    }
  });

  return methods;
}

/**
 * Returns nested objects key value. Works with all combinations of nested objects (objects and arrays).
 * When an array is nested, the path should be passed as array index - example: [1]
 *
 * Example
 * let obj = {
 *  {12345: {id:12345, headers:'header html 12345'},
 *  {67890: {id:67890, headers:'header html 67890'},
 *  {11111: {id:11111, headers:'header html 111111'}
 * }
 *
 * getNestedObjectValue(obj,['67890', 'header'])  returns 'header html 67890'
 * getNestedObjectValue(obj,['AAAA', 'header'])  returns undefined
 */
export function getNestedObjectValue(nestedObject, pathToKey) {
  return pathToKey.reduce((obj, key) => {
    if (obj) {
      return obj[key];
    }
  }, nestedObject);
}