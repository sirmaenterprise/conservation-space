import _ from 'lodash';
import {Component, View, Inject} from 'app/app';
import {Configurable} from 'components/configurable';
import {Eventbus} from 'services/eventbus/eventbus';
import {RecentObjectsService} from 'services/recent/recent-objects-service';
import {RecentObjectAddedEvent, RecentObjectUpdatedEvent, RecentObjectRemovedEvent} from './events';
import {InstanceRestService} from 'services/rest/instance-service';
import {HEADER_DEFAULT} from 'instance-header/header-constants';
import 'instance/instance-list';

import template from './recent-objects-list.html!text';

const EMPTY_LIST_MESSAGE = 'recent.objects.none';
const LOADING_OBJECT_MESSAGE = 'recent.objects.load';

@Component({
  selector: 'seip-recent-objects-list',
  properties: {
    config: 'config',
    typesFilter: 'types-filter',
    selectedItems: 'selected-items'
  }
})
@View({
  template
})
@Inject(Eventbus, RecentObjectsService, InstanceRestService)
export class RecentObjectsList extends Configurable {

  constructor(eventbus, recentObjectsService, instanceRestService) {
    super({
      selectableItems: false,
      singleSelection: false,
      selectionHandler: _.noop,
      emptyListMessage: EMPTY_LIST_MESSAGE,
      loadingListMessage: LOADING_OBJECT_MESSAGE
    });

    this.eventbus = eventbus;
    this.recentObjectsService = recentObjectsService;
    this.instanceRestService = instanceRestService;
  }

  ngOnInit() {
    this.instances = [];
    this.filterRecentObjects();

    this.events = [
      this.eventbus.subscribe(RecentObjectAddedEvent, this.updateList.bind(this)),
      this.eventbus.subscribe(RecentObjectUpdatedEvent, this.updateList.bind(this)),
      this.eventbus.subscribe(RecentObjectRemovedEvent, this.removeFromList.bind(this))
    ];
  }

  updateList(instance) {
    if (this.config.identifiersFilter) {
      // apply the filter to the incoming instance and validate
      this.config.identifiersFilter([instance.id]).then(instances => {
        // validate that the filter has not filtered out the instance
        if (instances.length === 1 && instances[0].id === instance.id) {
          this.insertToList(instance);
        }
      });
    } else {
      // nothing to filter here
      this.insertToList(instance);
    }
  }

  insertToList(instance) {
    if (this.config.filterByWritePermissions && !instance.writeAllowed) {
      return;
    }
    if (!instance.headers || _.isEmpty(instance.headers)) {
      this.instanceRestService.load(instance.id, {
        params: {
          properties: [HEADER_DEFAULT]
        }
      }).then(response => {
        instance.headers = response.data.headers;
        this.recentObjectsService.insertRecentObject(this.instances, instance);
      });
    } else {
      this.recentObjectsService.insertRecentObject(this.instances, instance);
    }
  }

  removeFromList(instance) {
    this.recentObjectsService.removeRecentObjectFromList(this.instances, instance);
  }

  ngOnDestroy() {
    for (let event of this.events) {
      event.unsubscribe();
    }
  }

  shouldRenderList() {
    return this.instances || this.identifiers;
  }

  filterRecentObjects() {
    let identifiers = this.recentObjectsService.getRecentObjects();
    if (identifiers.length < 1) {
      return;
    }

    if (!this.config.identifiersFilter) {
      let propertiesToLoad = this.config.propertiesToLoad || [HEADER_DEFAULT];
      this.instanceRestService.loadBatch(identifiers, {params: {properties: propertiesToLoad}}).then((response) => {
        this.instances = this.filterByWritePermissions(response.data);
      });
    } else {
      // filter and fetch the recent objects using the identifiers and assign to instances
      this.config.identifiersFilter(identifiers).then(instances => this.instances = this.filterByWritePermissions(instances));
    }
  }

  filterByWritePermissions(instances) {
    if (this.config.filterByWritePermissions) {
      return instances.filter(instance => instance.writeAllowed);
    }
    return instances;
  }
}