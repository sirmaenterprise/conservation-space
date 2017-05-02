import _ from 'lodash';

function mock$scope() {
  let scope = {
    watchers: []
  };

  scope.$watch = scope.$watchCollection = (watcher, callback) => {
    let previousValue = undefined;
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

  return scope;
}

export {mock$scope as mock$scope};