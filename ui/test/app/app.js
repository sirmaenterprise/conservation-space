//stubs for the application main module
var application = {
  service: function () {
  },
  directive: function () {
  }
};

export default application;

application.factory = function () {
};

export function Inject() {
  return function (target) {

  }
}

export function Injectable(target) {
  return function (target) {

  }
}

export function HttpInterceptor(target) {

}

export function View(config) {
  return function (target) {

  }
}

export function Component(config) {
  return function (target) {

  }
}

export var NgElement = {
  name: '$element'
}

export function Event(name) {
  var argsCount = arguments.length;
  if (argsCount > 1) {
    throw new Error('An Event can accept only the event name as argument!');
  }

  var event, parts, context,
    argument = arguments[0];

  if (typeof argument === 'function') {
    event = getComponentName(argument);
  } else if (typeof argument === 'string' && argument.trim().length > 0) {
    parts = argument.trim().split(':');
    if (parts.length === 2) {
      context = parts[0];
    }
    event = argument.replace(':', '.');
  } else if (argsCount === 1 && typeof argument !== 'function' && typeof argument !== 'string') {
    throw new TypeError('Unsupported Event argument: expected "string" or "function" but found [' + typeof argument + ']');
  }

  return function (target) {
    event = event || getComponentName(target);
    if (context) {
      target.CONTEXT = context;
    }
    if (event) {
      target.EVENT_NAME = event;
    }
    return target;
  };
}

export function Filter(target) {

}

export function getComponentName(target) {
  var className = getClassName(target);
  return className.charAt(0).toLowerCase() + className.substr(1);
}

export function getClassName(target) {
  var className = target.name;

  // for some reason the classes don't have name in IE. Let's pollyfill it
  if (!target.name) {
    className = /^function\s+([\w\$]+)\s*\(/.exec(target.toString())[1];
    target.name = className;
  }

  return className;
};
