import _ from 'lodash';

function mock$scope() {
  let scope = {
    watchers: []
  };

  let watchHandler = (watchSingleValue) => {
    return (watcher, callback) => {
      let previousValue = watchSingleValue ? undefined : [];
      let watcherFunc = () => {
        let newValue = watcher();
        if (!_.isEqual(newValue, previousValue)) {
          callback(newValue, previousValue);
          previousValue = newValue;
        }
      };
      scope.watchers.push(watcherFunc);
      return () => {
        let watcherIndex = scope.watchers.indexOf(watcherFunc);
        if (watcherIndex > -1) {
          scope.watchers.splice(watcherIndex, 1);
        }
      };
    };
  };

  scope.$watch = watchHandler(true);
  scope.$watchCollection = watchHandler(false);

  scope.$digest = () => {
    scope.watchers.forEach((watcher) => {
      watcher();
    });
  };

  scope.$destroy = () => { };

  scope.$new = () => {
    return scope;
  };

  scope.$apply = () => { };

  scope.$evalAsync = (func) => {
    func();
  };

  return scope;
}

export {mock$scope as mock$scope};