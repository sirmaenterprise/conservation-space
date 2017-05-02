import 'angular-ui-router';
import {Inject, Injectable, getClassName} from 'app/app';
import {Eventbus} from 'services/eventbus/eventbus';
import {RouterStateChangeStartEvent} from 'common/router/router-state-change-start-event';
import {RouterStateChangeSuccessEvent} from 'common/router/router-state-change-success-event';
import {PluginsService} from 'services/plugin/plugins-service';
import {DialogService} from 'components/dialog/dialog-service';
import {TranslateService} from 'services/i18n/translate-service';
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
@Inject('$state', '$rootScope', Eventbus, PluginsService, DialogService, TranslateService, PromiseAdapter)
export class Router {

  constructor($state, $rootScope, eventbus, pluginsService, dialogService, translateService, promiseAdapter) {
    this.$state = $state;
    this.eventbus = eventbus;
    this.$rootScope = $rootScope;
    this.pluginsService = pluginsService;
    this.dialogService = dialogService;
    this.translateService = translateService;
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
      let confirmedNavigation = this.promiseAdapter.promise((resolve, reject) => {
        let shouldBeInterrupted = !fromParams.skipRouteInterrupt && this.shouldInterrupt();
        if (shouldBeInterrupted) {
          let dialogOpts = {
            showClose: true,
            buttons: [
              {
                id: DialogService.OK,
                label: this.translateService.translateInstant('router.interrupt.dialog.btn.leave'),
                cls: 'btn-primary'
              },
              {
                id: DialogService.CANCEL,
                label: this.translateService.translateInstant('router.interrupt.dialog.btn.stay')
              }
            ],
            onButtonClick: (buttonId, componentScope, dialogConfig) => {
              if (buttonId === DialogService.CANCEL) {
                reject();
                delete toState.resolve.pauseStateChange;
                dialogConfig.dismiss();
              } else {
                resolve();
              }
            }
          };
          this.dialogService.confirmation(this.translateService.translateInstant('router.interrupt.dialog.message'), this.translateService.translateInstant('router.interrupt.dialog.header'), dialogOpts);
        } else {
          resolve();
        }
      });

      return confirmedNavigation.then(() => {
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

  navigate(state, params, options) {
    if (options) {
      this.$state.params.skipRouteInterrupt = options.skipRouteInterrupt;
    }
    this.$state.go(state, params, options);
  }

  getCurrentState() {
    return this.$state.current.name;
  }

  /**
   * Gets the internal (hash) Url for a given router state.
   *
   * @returns {*} the escaped version of the url. I.e. : is replaces with %3A.
   */
  getStateUrl(state) {
    return this.$state.href(state, {});
  }
}
