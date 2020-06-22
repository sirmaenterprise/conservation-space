import 'jquery';
import angular from 'angular';
import _ from 'lodash';

var application = angular.module('app', []);

// enable lazy loading
// inspired by -
// http://www.bennadel.com/blog/2553-loading-angularjs-components-after-your-application-has-been-bootstrapped.htm
application.config(['$controllerProvider', '$provide', '$compileProvider', '$filterProvider', '$httpProvider', function ($controllerProvider, $provide, $compileProvider, $filterProvider, $httpProvider) {
  application._controller = application.controller;
  application._service = application.service;
  application._factory = application.factory;
  application._value = application.value;
  application._directive = application.directive;
  application._filter = application.filter;

  application.service = function (name, constructor) {
    $provide.service(name, constructor);
    return this;
  };

  application.directive = function (name, factory) {
    $compileProvider.directive(name, factory);
    return this;
  };

  application.filter = function (name, factory) {
    $filterProvider.register(name, factory);
    return this;
  };

  application.$httpProvider = $httpProvider;

  // Configure $http service to combine processing of multiple http responses received at around
  // the same time via $rootScope.$applyAsync. This can result in significant performance improvement
  // for bigger applications that make many HTTP requests concurrently (common during application bootstrap).
  // see https://docs.angularjs.org/api/ng/provider/$httpProvider
  application.$httpProvider.useApplyAsync(true);

  application.interceptors.forEach((value) => {
    application.$httpProvider.interceptors.push(value);
  });
}
]);

export function Inject() {
  var _arguments = _.map(arguments, function (element) {
    if (_.isString(element)) {
      return element;
    }
    // the argument is ES6 class
    return getClassName(element);
  });

  return function (target) {
    target.$inject = _arguments;
  };
}

export function Injectable() {
  return function (target) {
    application.service(getClassName(target), target);
  };
}

export function View(config) {
  return function (target) {
    target.$view = config.template;
  };
}

application.interceptors = [];

export function HttpInterceptor(target) {
  var className = getClassName(target);

  application.service(className, target);
  // since $http is a services, a single instance is created during app startup
  // adding interceptors after application bootstartup has no effect
  if (!application.$httpProvider) {
    application.interceptors.push(className);
  } else {
    throw new Error('Cannot add interceptor after application startup');
  }
  return target;
}

export function Component(config) {
  return function (target) {
    var controllerName = getComponentName(target);

    var attributeBindings = {};

    if (config.properties) {
      Object.keys(config.properties).forEach(function (property) {
        // attributes should be declared as optional because of https://docs.angularjs.org/error/$compile/nonassign
        attributeBindings[property] = '=?';
      });
    }

    if (config.events) {
      _.forEach(config.events, function (event) {
        attributeBindings[event] = '&';
      });
    }

    target.COMPONENT_SELECTOR = config.selector;

    // Attribute selectors are wrapped in [] (css style).
    var restriction = 'E';
    var selector = config.selector;

    // restrict directive only to attributes if an attribute selector is passed
    if (selector.charAt(0) === '[') {
      restriction = 'A';
      selector = selector.substring(1, selector.length - 1);
    }

    if (selector.charAt(0) === '.') {
      restriction = 'C';
      selector = selector.substring(1, selector.length);
    }

    if (target.prototype.ngOnInit) {
      target.prototype.$onInit = target.prototype.ngOnInit;
    }

    if (target.prototype.ngOnDestroy) {
      target.prototype.$onDestroy = target.prototype.ngOnDestroy;
    }

    application.directive(_.camelCase(selector), function () {
      var definitionObject = {
        template: target.$view,
        replace: true,
        controllerAs: controllerName,
        bindToController: attributeBindings,
        controller: target,
        restrict: restriction,
        transclude: config.transclude,
        link(scope, element, attrs, controller) {
          if (controller.ngAfterViewInit) {
            // the view gets compiled in the current event loop turn and is available on the next
            application.$timeout(function () {
              controller.ngAfterViewInit.call(controller);
            }, 0);
          }
          // manually detach the controller from the scope on destroy and remove the event listeners and watchers
          // this prevents the controller from leaking in a case of memory leak
          scope.$on('$destroy', function () {
            delete scope[controllerName];
            delete scope['$$listeners'];
            delete scope['$$watchers'];

            // To avoid memory leaks, jQuery removes other constructs such as data and event handlers from the child elements before removing the elements themselves.
            $(element).empty();

            $(element).remove();
          });
        }
      };

      // isolated scope is needed for element components
      if (restriction === 'E' || restriction === 'C') {
        definitionObject.scope = {};
      } else {
        definitionObject.scope = true;
      }

      return definitionObject;
    });
    return target;
  };
}

export function Event() {
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
  } else if (argsCount === 1 && typeof argument !== 'string') {
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
  var functionArgs = target.$inject || [];
  var factoryFunction = (...args) => {
    var instance = new target(...args);
    return function (text, additionalArgs) {
      return instance.filter(text, additionalArgs);
    };
  };

  functionArgs.push(factoryFunction);

  application.filter(getComponentName(target), functionArgs);
  return target;
}

export function getComponentName(target) {
  var className = getClassName(target);
  return className.charAt(0).toLowerCase() + className.substr(1);
}

export function getClassName(target) {
  var className = target.name;

  // for some reason the classes don't have name in IE and a polyfill is required
  if (_.isFunction(target)) {
    className = /^function\s+([\w\$]+)\s*\(/.exec(target.toString())[1];
    target.name = className;
  }

  return className;
}

application.run(['$rootScope', '$compile', '$timeout', function ($rootScope, $compile, $timeout) {
  $rootScope.pluginRegistry = PluginRegistry;
  application.$rootScope = $rootScope;
  application.$compile = $compile;
  application.$timeout = $timeout;
}]);

application.directive('extensionPoint', ['$compile', '$q', function ($compile, $q) {
  return {
    restrict: 'A',
    link(scope, element, attributes) {
      var extensions = PluginRegistry.get(attributes.extensionPoint);
      var components = PluginRegistry.get('components');

      // collect list of all component modules and load them
      var modulesToLoad = [];

      if (!extensions) {
        extensions = [];
        console.warn('No plugins plugged in', attributes.extensionPoint, 'extension point');
      }

      extensions.forEach(function (extension) {
        let module = extension.module;
        if (_.isUndefined(module)) {
          var currentComponent = _.find(components, function (component) {
            return extension.component === component.name;
          });
          module = currentComponent.module;
        }

        if (!_.isUndefined(module)) {
          modulesToLoad.push(module);
        }
      });

      var attributesString = '';
      if (attributes.extensionProperties) {
        attributes.extensionProperties.split(',').forEach(function (attribute) {
          attributesString += attribute + '="' + attributes[attribute] + '" ';
        });
      }

      var promises = modulesToLoad.map(name => System.import(name));

      $q.all(promises).then(function () {
        var concatenated = '';

        for (let extension of extensions) {
          concatenated += '<' + extension.component + ' ' + attributesString + '>' + '</' + extension.component + '>';
        }

        var compiled = $compile(concatenated)(scope);

        for (let i = 0; i < compiled.length; i++) {
          element.append(compiled[i]);
        }
      }, function (err) {
        console.error(err);
      });
    }
  };
}]);

export var NgElement = {
  name: '$element'
};

export var NgScope = {
  name: '$scope'
};

export var NgRootScope = {
  name: '$rootScope'
};

export var NgTimeout = {
  name: '$timeout'
};

export var NgInterval = {
  name: '$interval'
};

export var NgCompile = {
  name: '$compile'
};

export var NgHttp = {
  name: '$http'
};

export var NgFilter = {
  name: '$filter'
};

export var NgDocument = {
  name: '$document'
};

export var NgState = {
  name: '$state'
};

export var NgInjector = {
  name: '$injector'
};

export default application;
