import {Event} from 'app/app';

/**
 * Base event for all recent objects events.
 */
class BaseRecentObjectEvent {
  constructor(instance) {
    this.instance = instance;
  }

  getData() {
    return this.instance;
  }
}

/**
 * Fired when an objects is added to the list of recently used objects.
 */
@Event()
export class RecentObjectAddedEvent extends BaseRecentObjectEvent {
}

/**
 * Fired when an existing recent object has been updated and moved to the top of the list.
 */
@Event()
export class RecentObjectUpdatedEvent extends BaseRecentObjectEvent {
}

/**
 * Fired when a recent object is deleted and removed from the list.
 *
 * This is not fired when the list exceeds its max size and an object is removed from it!
 */
@Event()
export class RecentObjectRemovedEvent extends BaseRecentObjectEvent {
}