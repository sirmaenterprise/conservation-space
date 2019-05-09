import {Injectable, Inject} from 'app/app';
import {Configuration} from 'common/application-config';
import {Eventbus} from 'services/eventbus/eventbus';
import {UserService} from 'security/user-service';
import {LocalStorageService} from 'services/storage/local-storage-service';
import {InstanceUtils} from 'instance/utils';
import {RecentObjectAddedEvent, RecentObjectUpdatedEvent, RecentObjectRemovedEvent} from 'recent-objects/events';
import _ from 'lodash';

/**
 * Service for managing recently used objects.
 *
 * The recently used objects are mapped per user ID and stored in the browser's local store.
 *
 * The size of the recently used objects is based on {@link Configuration.USER_RECENT_OBJECTS_SIZE} value and they are
 * updated on service initialization, on add & remove.
 *
 * Service is publishing 3 different events:
 * 1) RecentObjectAddedEvent - when a recent object is added to the store
 * 2) RecentObjectUpdatedEvent - when an existing recent object is updated to be on top
 * 3) RecentObjectRemovedEvent - when a recent object is removed from the store. This is not fired for recent
 *                               object removed on list size reduce/update!
 *
 * To get the identifiers of stored recent objects for the currently logged user, use {@link #getRecentObjects()}
 *
 * The service provides two convenient utility methods to update lists of instantiated recent objects with added/updated
 * or removed recent object:
 * 1) {@link #insertRecentObject()}
 * 2) {@link #removeRecentObjectFromList}
 *
 * Use {@link #addRecentObject()} to add an instance as recently used object and {@link #removeRecentObject} to remove.
 * The service will not consider any versions of an instance or the currently logged user as recent objects.
 *
 * @author Mihail Radkov
 */
@Injectable()
@Inject(Eventbus, Configuration, UserService, LocalStorageService)
export class RecentObjectsService {

  constructor(eventbus, configuration, userService, localStorageService) {
    this.eventbus = eventbus;
    this.configuration = configuration;
    this.userService = userService;
    this.localStorageService = localStorageService;

    this.userService.getCurrentUser().then((user) => {
      this.currentUserId = user.id;
      this.reduceRecentObjects();
    });
  }

  reduceRecentObjects() {
    var objects = this.getRecentObjects();
    let maxSize = this.getMaxListSize();
    if (objects.length > maxSize) {
      objects.splice(maxSize);
      this.setRecentObjects(objects);
    }
  }

  /**
   * Adds the provided instance to the recently used objects as long as it is not a version or the current user.
   *
   * If the instance wasn't in the list already, a RecentObjectUpdatedEvent is fired or RecentObjectAddedEvent if it was.
   *
   * @param instance - the instance to add, cannot be null and must have a legit identifier!
   */
  addRecentObject(instance) {
    if (this.isCurrentUser(instance) || InstanceUtils.isVersion(instance)) {
      return;
    }

    let objects = this.getRecentObjects();
    let index = objects.indexOf(instance.id);
    this.updateListWithObject(objects, instance.id, index);
    this.setRecentObjects(objects);

    if (index > -1) {
      this.eventbus.publish(new RecentObjectUpdatedEvent(instance));
    } else {
      this.eventbus.publish(new RecentObjectAddedEvent(instance));
    }
  }

  /**
   * Removes the provided instance from the recent objects.
   *
   * If the instance was present in the recent objects, a RecentObjectRemovedEvent is fired.
   *
   * @param instance - the instance to remove, cannot be null and must have a legit identifier!
   */
  removeRecentObject(instance) {
    let objects = this.getRecentObjects();
    let index = objects.indexOf(instance.id);

    if (index > -1) {
      objects.splice(index, 1);
      this.setRecentObjects(objects);
      this.eventbus.publish(new RecentObjectRemovedEvent(instance));
    }
  }

  /**
   * Inserts the provided instance into the given list of instances. If the instance was previously present,
   * it's moved on top.
   *
   * @param list - the list to be updated, cannot be null
   * @param instance - the instance to insert, cannot be null and must have a legit identifier!
   */
  insertRecentObject(list, instance) {
    let index = _.findIndex(list, (recentObject) => {
      return recentObject.id === instance.id;
    });

    this.updateListWithObject(list, instance, index);
  }

  /**
   * Removes the provided instance from the given list if it exists.
   *
   * @param list - the list to be updated, cannot be null
   * @param instance - the instance to remove, cannot be null and must have a legit identifier!
   */
  removeRecentObjectFromList(list, instance) {
    let index = _.findIndex(list, recent => recent.id === instance.id);
    if (index > -1) {
      list.splice(index, 1);
    }
  }

  updateListWithObject(list, object, objectIndex) {
    if (objectIndex > -1) {
      list.splice(objectIndex, 1);
    } else {
      list.splice(this.getMaxListSize() - 1);
    }
    list.unshift(object);
  }

  /**
   * Retrieves the list of recently used objects identifiers for the currently logged user.
   */
  getRecentObjects() {
    let objectsPerUser = this.localStorageService.getJson(LocalStorageService.RECENT_OBJECTS, {});
    let userObjects = objectsPerUser[this.currentUserId];
    if (!userObjects) {
      userObjects = [];
      objectsPerUser[this.currentUserId] = userObjects;
    }
    return userObjects;
  }

  setRecentObjects(objects) {
    let objectsPerUser = this.localStorageService.getJson(LocalStorageService.RECENT_OBJECTS, {});
    objectsPerUser[this.currentUserId] = objects;
    this.localStorageService.set(LocalStorageService.RECENT_OBJECTS, objectsPerUser);
  }

  getMaxListSize() {
    return this.configuration.get(Configuration.USER_RECENT_OBJECTS_SIZE);
  }

  isCurrentUser(instance) {
    return instance.id === this.currentUserId;
  }
}
