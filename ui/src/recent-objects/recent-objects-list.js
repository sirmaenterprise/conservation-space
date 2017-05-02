import _ from 'lodash';
import {Component, View, Inject} from 'app/app';
import {Configurable} from 'components/configurable';
import {UserService} from 'services/identity/user-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {LocalStorageService} from 'services/storage/local-storage-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {RecentObjectAddedEvent} from './events';
import {AfterIdocDeleteEvent} from 'idoc/actions/events/after-idoc-delete-event';
import 'instance/instance-list';
import template from './recent-objects-list.html!text';

const EMPTY_LIST_MESSAGE = 'recent.objects.none';
const LOADING_OBJECT_MESSAGE = 'recent.objects.load';

@Component({
  selector: 'seip-recent-objects-list',
  properties: {
    config: 'config',
    selectedItems: 'selected-items',
    typesFilter: 'types-filter'
  }
})
@View({
  template: template
})
@Inject(LocalStorageService, UserService, Eventbus, InstanceRestService)
export class RecentObjectsList extends Configurable {

  constructor(localStorageService, userService, eventbus, instanceService) {
    super({
      selectableItems: false,
      singleSelection: false,
      selectionHandler: _.noop,
      emptyListMessage: EMPTY_LIST_MESSAGE,
      loadingListMessage: LOADING_OBJECT_MESSAGE
    });

    this.eventbus = eventbus;
    this.instanceService = instanceService;
    this.instances = [];

    userService.getCurrentUser().then((user) => {
      this.identifiers = localStorageService.getJson(LocalStorageService.RECENT_OBJECTS, {})[user.id] || [];
    });
  }

  ngOnInit() {
    this.recentObjectAddedHandler = this.eventbus.subscribe(RecentObjectAddedEvent, this.updateList.bind(this));
    this.recentObjectRemovedHandler = this.eventbus.subscribe(AfterIdocDeleteEvent, this.removeFromList.bind(this));
  }

  updateList(id) {
    this.instanceService.load(id).then((response) => this.instances.unshift(response.data));
  }

  removeFromList(payload) {
    let found = _.find(this.instances, payload);
    _.pull(this.instances, found);
  }

  ngOnDestroy() {
    if (this.recentObjectAddedHandler) {
      this.recentObjectAddedHandler.unsubscribe();
    }

    if(this.recentObjectRemovedHandler){
      this.recentObjectRemovedHandler.unsubscribe();
    }
  }
}