import 'angular-ui-router';
import {Inject, Injectable, getClassName, NgRootScope, NgState} from 'app/app';
import {Eventbus} from 'services/eventbus/eventbus';
import {RouterStateChangeStartEvent} from 'common/router/router-state-change-start-event';
import {RouterStateChangeSuccessEvent} from 'common/router/router-state-change-success-event';
import {PluginsService} from 'services/plugin/plugins-service';
import {ConfirmationDialogService} from 'components/dialog/confirmation-dialog-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

export default function (angularModule, routes) {
  angularModule.requires.push('ui.router');

  var loadModule = function ($q, src) {
    var def = $q.defer();

    System.import(src).then(() => {
      return def.resolve();
    });

    return def.promise;
  };

  var RouterConfig = ['$stateProvider',
    function ($stateProvider) {

      routes.forEach(function (state) {
        $stateProvider.state(state.stateName, {
          url: state.url,
          template: '<' + state.component + '>' + '</' + state.component + '>',

          resolve: {
            module: ['$q', function ($q) {
              if (typeof state.title !== 'undefined') {
                document.title = state.title;
              }

              if (typeof state.module === 'undefined') {
                throw 'No module provided for route with name ' + state.stateName;
              }
              return loadModule($q, state.module);
            }]
          },
          //require Router service in order to subscribe to the first $stateChangeSuccess event
          onEnter: [getClassName(Router), function () {
          }]
        });
      });
    }
  ];

  return RouterConfig;
};

@Injectable()
@Inject(NgState, NgRootScope, Eventbus, PluginsService, ConfirmationDialogService, PromiseAdapter)
export class Router {

  constructor($state, $rootScope, eventbus, pluginsService, confirmationDialogService, promiseAdapter) {
    this.$state = $state;
    this.eventbus = eventbus;
    this.$rootScope = $rootScope;
    this.pluginsService = pluginsService;
    this.confirmationDialogService = confirmationDialogService;
    this.promiseAdapter = promiseAdapter;
    this.$rootScope.$on('$stateChangeStart', this.onStateChangeStart.bind(this));
    this.$rootScope.$on('$stateChangeSuccess', this.onStateChangeSuccess.bind(this));

    this.pluginsService.loadPluginServiceModules('route-interrupter').then((modules) => {
      this.registeredRouteInterrupters = modules;
    });
  }

  onStateChangeStart(event, toState, toParams, fromState, fromParams) {
    if (!toState.resolve) {
      toState.resolve = {};
    }
    toState.resolve.pauseStateChange = () => {
      let confirmedNavigationPromise = this.promiseAdapter.promise((resolve, reject) => {
        let shouldBeInterrupted = !fromParams.skipRouteInterrupt && this.shouldInterrupt();
        if (shouldBeInterrupted) {
          this.confirmationDialogService.confirm({
            message: 'router.interrupt.dialog.message',
            header: 'router.interrupt.dialog.header',
            confirmLabel: 'router.interrupt.dialog.btn.leave',
            cancelLabel: 'router.interrupt.dialog.btn.stay'
          }).then(() => {
            resolve();
          }).catch(() => {
            reject();
            delete toState.resolve.pauseStateChange;
          });
        } else {
          resolve();
        }
      });

      return confirmedNavigationPromise.then(() => {
        this.eventbus.publish(new RouterStateChangeStartEvent(arguments));
        delete toState.resolve.pauseStateChange;
      });
    };
  }

  shouldInterrupt() {
    if (this.registeredRouteInterrupters) {
      return (Object.keys(this.registeredRouteInterrupters)).some((routeInterrupterName) => {
        return this.registeredRouteInterrupters[routeInterrupterName].shouldInterrupt(this);
      });
    }
    return false;
  }

  onStateChangeSuccess(event, toState, toParams, fromState, fromParams) {
    this.eventbus.publish(new RouterStateChangeSuccessEvent(arguments));
  }

  /**
   * This method triggers a navigation to a given state with given parameters
   * and optionally provided options. Example parameters to be provided
   *
   * @param state the state to which to navigate
   * @param params query parameters passed to the state
   * @param options optional, provide behavior for the navigation process
   *
   * Currently supported Options & Parameters:
   *
   * options: {
   *  inherit: true | false - specifies if existing state params should be inherited
   *  reload: true | false - forces or disables page reload when state or/and params are changed,
   *                         furthermore RouterStateChangeSuccessEvent event is always fired
   * };
   */
  navigate(state, params, options) {
    if (options) {
      this.$state.params.skipRouteInterrupt = options.skipRouteInterrupt;
      options = this.prepareOptions(state, params, options);
    }
    this.$state.go(state, params, options);
  }

  getStateByName(state) {
    return this.$state.get(state);
  }

  getCurrentState() {
    return this.$state.current.name;
  }

  getCurrentParams() {
    return this.$state.params;
  }

  /**
   * Gets the internal (hash) Url for a given router state.
   *
   * @returns {*} the escaped version of the url. I.e. : is replaces with %3A.
   */
  getStateUrl(state) {
    return this.$state.href(state, {});
  }

  /**
   * Processes provided router navigation options
   * before they are passed to the internal $state
   * for the actual page navigation.
   *
   * @param options the options to be processed
   */
  prepareOptions(state, params, options) {
    // artificially process reload state
    if (options.reload === false) {
      // avoid page reloading
      options.notify = false;
      // leverage notify: false which does not reload or trigger $stateChangeSuccess by default, so trigger it manually
      this.onStateChangeSuccess('$stateChangeSuccess', this.getStateByName(state), params, this.getCurrentState(), this.getCurrentParams());
    }
    return options;
  }
}
