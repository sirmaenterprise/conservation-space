/**
 * Performs type-safe stubbing of a given class. Includes stubs for all the methods of the class.
 *
 * @param type class for which a stubbed instance will be created.
 * @returns {{}} instance of the provided type with all methods stubbed.
 */
export function stub(type) {
  var result = {};

  getClassMethods(type).forEach(function (method) {
    result[method] = sinon.stub()
  });

  Object.seal(result);

  return result;
}

function getClassMethods(obj) {
  var props = new Set();

  do {
    if (obj.name) {
      Object.getOwnPropertyNames(obj.prototype).forEach(function (prop) {
        if(typeof obj.prototype[prop] === 'function') {
          props.add(prop);
        }
      });
    }
  } while (obj = Object.getPrototypeOf(obj));

  return props;
}