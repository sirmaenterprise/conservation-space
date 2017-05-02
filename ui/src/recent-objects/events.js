import {Event} from 'app/app';

/**
 * Fired when an objects is added to the list of recently used objects.
 */
@Event()
export class RecentObjectAddedEvent {
  constructor(objectId) {
    this.objectId = objectId;
  }

  getData() {
    return this.objectId;
  }
}
