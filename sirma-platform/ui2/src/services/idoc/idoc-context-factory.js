import {Injectable, Inject} from 'app/app';
import {Router} from 'adapters/router/router';
import {IdocContext} from 'idoc/idoc-context';
import {InstanceRestService} from 'services/rest/instance-service';
import {SessionStorageService} from 'services/storage/session-storage-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {Eventbus} from 'services/eventbus/eventbus';
import {RouterStateChangeStartEvent} from 'common/router/router-state-change-start-event';
import {PluginsService} from 'services/plugin/plugins-service';

/**
 * Creates instances of idoc context. It can be used to obtain current context outside of the idoc view.
 */
@Injectable()
@Inject(InstanceRestService, SessionStorageService, PromiseAdapter, Eventbus, Router, PluginsService)
export class IdocContextFactory {
  constructor(instanceRestService, sessionStorageService, promiseAdapter, eventbus, router, pluginsService) {
    this.instanceRestService = instanceRestService;
    this.pluginsService = pluginsService;
    this.sessionStorageService = sessionStorageService;
    this.promiseAdapter = promiseAdapter;
    this.eventbus = eventbus;
    this.router = router;

    // clear the current context
    eventbus.subscribe(RouterStateChangeStartEvent, () => this.clearCurrentContext());
  }

  /**
   * Creates new context which overrides current context
   * @param id
   * @param mode
   * @returns {*}
   */
  createNewContext(id, mode) {
    this.context = new IdocContext(id, mode, this.instanceRestService, this.sessionStorageService,
      this.promiseAdapter, this.eventbus, this.router, this.pluginsService);
    return this.context;
  }

  getCurrentContext() {
    return this.context;
  }

  clearCurrentContext() {
    this.context = undefined;
  }
}
