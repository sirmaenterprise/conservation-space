import {View, Component, Inject} from 'app/app';
import {BreadcrumbEntryManager} from 'layout/breadcrumb/breadcrumb-entry-manager';
import {Eventbus} from 'services/eventbus/eventbus';
import {PluginsService} from 'services/plugin/plugins-service';
import {TranslateService} from 'services/i18n/translate-service';
import {EntryItem} from 'layout/breadcrumb/breadcrumb-entry/entry-item';
import {InstanceRestService} from 'services/rest/instance-service';
import {BreadcrumbStateHandler} from 'layout/breadcrumb/breadcrumb-state-handler';
import {RouterStateChangeSuccessEvent} from 'common/router/router-state-change-success-event';
import {EntrySelectedFromBreadcrumbEvent} from 'layout/breadcrumb/breadcrumb-entry/entry-selected-event';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {HEADER_BREADCRUMB} from 'instance-header/header-constants';
import 'layout/breadcrumb/breadcrumb-entry/entry-component';
import 'components/http-requests-indicator/http-requests-indicator';
import _ from 'lodash';

import './breadcrumb.css!';
import 'font-awesome/css/font-awesome.css!';
import template from './breadcrumb.html!text';

const BREADCRUMB_INSTANCE_PROPERTIES = {
  properties: ['title', HEADER_BREADCRUMB]
};

@Component({
  selector: 'seip-breadcrumb'
})
@View({
  template
})
@Inject(Eventbus, BreadcrumbEntryManager, StateParamsAdapter, WindowAdapter, '$state', PluginsService, TranslateService, InstanceRestService)
export class Breadcrumb {

  constructor(eventbus, breadcrumbEntryManager, stateParamsAdapter, windowAdapter, $state, pluginsService, translateService, instanceRestService) {
    this.$state = $state;
    this.eventbus = eventbus;
    this.pluginsService = pluginsService;
    this.translateService = translateService;
    this.stateParamsAdapter = stateParamsAdapter;
    this.windowAdapter = windowAdapter;
    this.breadcrumbEntryManager = breadcrumbEntryManager;
    this.instanceRestService = instanceRestService;
  }

  ngOnInit() {
    this.breadcrumbs = this.breadcrumbEntryManager.getEntries();
    this.routesPromise = this.pluginsService.loadComponentModules('route', 'stateName');
    this.updateBreadcrumb(this.$state.current, this.stateParamsAdapter.getStateParams());

    this.events = [
      this.eventbus.subscribe(RouterStateChangeSuccessEvent, (event) => {
        // event[0][1] is the new state, event[0][2] are the new state parameters
        this.updateBreadcrumb(event[0][1], event[0][2]);
      }),
      this.eventbus.subscribe(EntrySelectedFromBreadcrumbEvent, (event) => {
        this.entryIndex = event[0];
      })
    ];
  }

  updateBreadcrumb(state, stateParams) {
    if (!state.name) {
      return;
    }
    if (this.entryIndex) {
      this.breadcrumbEntryManager.clear(this.entryIndex);
      this.entryIndex = null;
    }
    let currentStateUrl = this.getUrlFromState(state, stateParams);
    this.handleState(state.name, currentStateUrl, stateParams);
  }

  /**
   * Handle the ng-router state and execute state dependant actions.
   *
   * @param stateName The current opened ng-router state name
   * @param url The url matching provided state
   * @param stateParams the query parameters for the given state
   */
  handleState(stateName, url, stateParams) {
    if (BreadcrumbStateHandler.isResetState(stateName)) {
      // simply reset the breadcrumb entries
      this.breadcrumbEntryManager.clear();
      Breadcrumb.setBrowserTabName(this.windowAdapter, stateName);
    } else if (!BreadcrumbStateHandler.isIgnoreState(stateName)) {
      this.addState(stateName, url);
    } else if (stateParams.id) {
      this.addInstanceState(stateName, url, stateParams);
    }
  }

  addState(stateName, url) {
    if (this.routesPromise) {
      // fetch the routes from the plugin service
      this.routesPromise.then((routes) => {
        this.routes = routes;
        delete this.routesPromise;
        this.addStateEntry(stateName, url);
      });
    } else {
      // add state entry to breadcrumb
      this.addStateEntry(stateName, url);
    }
  }

  /**
   * Creates a breadcrumb state entry & appends it to the breadcrumb entries.
   *
   * @param stateName the name of the state
   * @param url the url to the state
   */
  addStateEntry(stateName, url) {
    let label = this.routes[stateName].label || stateName;
    let icon = this.routes[stateName].icon || 'fa-link';
    let title = this.translateService.translateInstant(label);
    let header = Breadcrumb.createHeader(title, url, icon);

    let entry = new EntryItem(stateName, header, stateName, true, url);
    this.breadcrumbEntryManager.add(entry);
  }

  addInstanceState(stateName, url, stateParams) {
    // extract the actual instance from the provided state param id
    this.instanceRestService.load(stateParams.id, {params: BREADCRUMB_INSTANCE_PROPERTIES}).then((response) => {
      let instance = response.data;
      let type = instance.instanceType;
      let header = instance.headers.breadcrumb_header;
      let entry = new EntryItem(stateParams.id, header, type, true, url);

      this.breadcrumbEntryManager.add(entry);
      let name = _.get(instance, 'properties.title', stateName);
      Breadcrumb.setBrowserTabName(this.windowAdapter, name);
    });
  }

  /**
   * Set the browser tab title if provided.
   *
   * @param windowAdapter
   * @param title
   */
  static setBrowserTabName(windowAdapter, title) {
    if (title) {
      windowAdapter.document.title = title;
    }
  }

  /**
   * Creates a simple breadcrumb header.
   *
   * @param stateTitle the header label/title
   * @param stateUrl the header url link
   * @param stateIcon the header icon
   */
  static createHeader(stateTitle, stateUrl, stateIcon) {
    let title = `<span data-property="title">${stateTitle}</span>`;
    let icon = `<span><i class="header-color fa fa-1x fa-fw ${stateIcon}"></i></span>`;
    return `${icon}<span><a class="instance-link" href="${stateUrl}">${title}</a></span>`;
  }

  getUrlFromState(state, params, options) {
    return this.$state.href(state, params, options);
  }

  ngOnDestroy() {
    for (let event of this.events) {
      event.unsubscribe();
    }
  }
}