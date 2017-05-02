import _ from 'lodash';
import {Injectable, Inject} from 'app/app';
import {Configuration} from 'common/application-config';
import {Eventbus} from 'services/eventbus/eventbus';
import {AfterIdocLoadedEvent} from 'idoc/events/after-idoc-loaded-event';
import {InstanceCreatedEvent} from 'idoc/events/instance-created-event';
import {ActionExecutedEvent} from 'services/actions/events';
import {UploadCompletedEvent} from 'file-upload/events';
import {SavedSearchLoadedEvent, SavedSearchCreatedEvent, SavedSearchUpdatedEvent} from 'search/components/saved/events';
import {LocalStorageService} from 'services/storage/local-storage-service';
import {UserService} from 'services/identity/user-service';
import {RecentObjectAddedEvent} from './events';
import {InstanceUtils} from 'instance/utils';

@Injectable()
@Inject(Configuration, Eventbus, LocalStorageService, UserService)
export class RecentObjectsListener {

  constructor(configuration, eventbus, localStorageService, userService) {
    this.configuration = configuration;
    this.localStorageService = localStorageService;
    this.eventbus = eventbus;

    userService.getCurrentUser().then((user) => {
      this.currentUserId = user.id;
      this.reduceObjectsSizeForUser();
    });

    eventbus.subscribe(AfterIdocLoadedEvent, this.handleIdocEvent.bind(this));
    eventbus.subscribe(InstanceCreatedEvent, this.handleIdocEvent.bind(this));
    eventbus.subscribe(ActionExecutedEvent, this.handleActionEvent.bind(this));
    eventbus.subscribe(UploadCompletedEvent, this.handleUploadEvent.bind(this));

    var savedSearchHandler = this.handleSavedSearchEvent.bind(this);
    eventbus.subscribe(SavedSearchLoadedEvent, savedSearchHandler);
    eventbus.subscribe(SavedSearchCreatedEvent, savedSearchHandler);
    eventbus.subscribe(SavedSearchUpdatedEvent, savedSearchHandler);
  }

  reduceObjectsSizeForUser() {
    var objects = this.localStorageService.getJson(LocalStorageService.RECENT_OBJECTS, {});
    var recentObjectsForUser = objects[this.currentUserId];
    if (recentObjectsForUser && recentObjectsForUser.length > this.getRecentObjectsSize()) {
      objects[this.currentUserId] = recentObjectsForUser.splice(0, this.getRecentObjectsSize());
      this.localStorageService.set(LocalStorageService.RECENT_OBJECTS, objects);
    }
  }

  handleIdocEvent(payload) {
    this.addRecentObject(payload[0].currentObject);
  }

  handleActionEvent(payload) {
    var data = payload.response && payload.response.data;
    if (!_.isArray(data)) {
      data = [payload.context.currentObject];
    }

    _.each(data, (instance) => this.addRecentObject(instance));
  }

  handleUploadEvent(payload) {
    this.addRecentObject(payload);
  }

  handleSavedSearchEvent(payload) {
    this.addRecentObject(payload);
  }

  addRecentObject(instance) {
    // versions shouldn't be added to recent objects
    if (InstanceUtils.isVersion(instance)) {
      return;
    }

    let id = instance.id;
    if (!id || id === this.currentUserId) {
      return;
    }

    var objects = this.localStorageService.getJson(LocalStorageService.RECENT_OBJECTS, {});
    var userObjects = objects[this.currentUserId];
    if (!userObjects) {
      userObjects = [];
      objects[this.currentUserId] = userObjects;
    }

    if (userObjects.indexOf(id) > -1) {
      return;
    }

    var recentObjectsSize = this.getRecentObjectsSize();
    if (userObjects.length === recentObjectsSize) {
      userObjects.pop();
    } else if (userObjects.length > recentObjectsSize) {
      userObjects.splice(recentObjectsSize - 1);
    }

    userObjects.unshift(id);
    this.localStorageService.set(LocalStorageService.RECENT_OBJECTS, objects);

    this.eventbus.publish(new RecentObjectAddedEvent(id));
  }

  getRecentObjectsSize() {
    return this.configuration.get(Configuration.USER_RECENT_OBJECTS_SIZE);
  }
}