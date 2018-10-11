import {Component, View, Inject} from 'app/app';
import {Configurable} from 'components/configurable';
import {Eventbus} from 'services/eventbus/eventbus';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {RecentObjectsService} from 'services/recent/recent-objects-service';
import {RecentObjectAddedEvent, RecentObjectUpdatedEvent, RecentObjectRemovedEvent} from 'recent-objects/events';
import {InstanceRestService} from 'services/rest/instance-service';
import {ContextualObjectsFactory} from 'services/context/contextual-objects-factory';
import {HEADER_BREADCRUMB} from 'instance-header/header-constants';

import './search-context-menu.css!css';
import template from './search-context-menu.html!text';

const INSTANCE_SERVICE_CONFIG = {params: {properties: [HEADER_BREADCRUMB]}};

/**
 * Context menu for rendering contextual objects.
 *
 * Contextual objects are as follows:
 * 1) current object - if enabled in the configuration
 * 2) recently used objects - if available
 *
 * Example configuration:
 *  {
 *    enableCurrentObject: true // toggles the visibility of the menu item
 *  }
 *
 * When a context is selected, the component will notify its parent with the onContextChange component event.
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'search-context-menu',
  properties: {
    'config': 'config'
  },
  events: ['onContextChange']
})
@View({
  template: template
})
@Inject(Eventbus, RecentObjectsService, InstanceRestService, PromiseAdapter, ContextualObjectsFactory)
export class SearchContextMenu extends Configurable {

  constructor(eventbus, recentObjectsService, instanceRestService, promiseAdapter, contextualObjectsFactory) {
    super({
      enableCurrentObject: true
    });

    this.eventbus = eventbus;
    this.recentObjectsService = recentObjectsService;
    this.instanceRestService = instanceRestService;
    this.promiseAdapter = promiseAdapter;
    this.contextualObjectsFactory = contextualObjectsFactory;
  }

  ngOnInit() {
    this.headerConfig = {
      preventLinkRedirect: true
    };

    this.loadRecentObjects().then((recentObjects) => {
      this.recentObjects = recentObjects;
      this.initialized = true;
    });

    this.events = [
      this.eventbus.subscribe(RecentObjectAddedEvent, this.updateList.bind(this)),
      this.eventbus.subscribe(RecentObjectUpdatedEvent, this.updateList.bind(this)),
      this.eventbus.subscribe(RecentObjectRemovedEvent, this.removeFromList.bind(this))
    ];
  }

  loadRecentObjects() {
    let recentObjectsIdentifiers = this.recentObjectsService.getRecentObjects();
    if (recentObjectsIdentifiers.length < 1) {
      return this.promiseAdapter.resolve([]);
    }

    return this.instanceRestService.loadBatch(recentObjectsIdentifiers, INSTANCE_SERVICE_CONFIG).then((response) => {
      return response.data;
    });
  }

  selectCurrentObject() {
    this.selectContext(this.contextualObjectsFactory.getCurrentObject());
  }

  selectContext(instance) {
    this.onContextChange({instance});
  }

  updateList(instance) {
    if (this.recentObjects) {
      if (instance.headers[HEADER_BREADCRUMB]) {
        // instance is ready to be rendered
        this.insertToList(instance);
      } else {
        // instance has no breadcrumb header present and should be fetched from the service
        this.instanceRestService.load(instance.id, INSTANCE_SERVICE_CONFIG).then((response) => {
          this.insertToList(response.data);
        });
      }
    }
  }

  removeFromList(instance) {
    if (this.recentObjects) {
      this.recentObjectsService.removeRecentObjectFromList(this.recentObjects, instance);
    }
  }

  insertToList(instance) {
    this.recentObjectsService.insertRecentObject(this.recentObjects, instance);
  }

  ngOnDestroy() {
    for (let event of this.events) {
      event.unsubscribe();
    }
  }
}