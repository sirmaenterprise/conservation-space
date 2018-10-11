/**
 * Performs type-safe stubbing of a given class. Includes stubs for all the methods of the class.
 *
 * @param type class for which a stubbed instance will be created.
 * @returns {{}} instance of the provided type with all methods stubbed.
 */
export function stub(type) {
  let result = {};

  getClassMethods(type).forEach(function (method) {
    result[method] = sinon.stub();
  });

  Object.seal(result);

  return result;
}

function getClassMethods(obj) {
  let props = new Set();

  do {
    if (obj.name) {
      Object.getOwnPropertyNames(obj.prototype).forEach(function (prop) {
        if (typeof obj.prototype[prop] === 'function') {
          props.add(prop);
        }
      });
    }
  } while (obj = Object.getPrototypeOf(obj));

  return props;
}

/**
 * Mock eventbus implementation so the real one is not used in test scenarios.
 */
export class MockEventbus {
  constructor() {
    this.subscriptions = {};
  }

  subscribe(type, callback) {
    if (this.subscriptions[type.name]) {
      this.subscriptions[type.name].push(callback);
    } else {
      this.subscriptions[type.name] = [callback];
    }
    return type.name;
  }

  publish(type) {
    this.subscriptions[type.constructor.name].forEach(callback => callback(type.data));
  }

  unsubscribe() {
  }

  reset() {
    this.subscriptions = {};
  }
}
