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

function getMethods(prototype) {
  var methods = new Set();

  Object.getOwnPropertyNames(prototype).forEach(function (property) {
    if (typeof prototype[property] === 'function') {
      methods.add(property);
    }
  });

  return methods;
}