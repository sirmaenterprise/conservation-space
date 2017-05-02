import {SharedObjectUpdatedEvent} from 'idoc/shared-object-updated-event';

/**
 * Every widget that holds some objects that might be changed as result of some executed actions must extend this class
 * and to implement a #refresh method. Everytime an object stored in idoc context is changed, an event is fired and the
 * the refresh method is invoked.
 *
 * The refresh method accepts an object containing:
 * - objectId: is the id of the object that has been updated
 */
export class Refreshable {

  constructor(eventbus) {
    if (typeof this.refresh !== 'function') {
      throw new Error('Must implement refresh method in component!');
    }
    this.eventbus = eventbus;
    this.sharedObjectUpdateEvent = eventbus.subscribe(SharedObjectUpdatedEvent, (data) => this.refresh(data[0]));
  }

  ngOnDestroy() {
    if (this.sharedObjectUpdateEvent) {
      this.sharedObjectUpdateEvent.unsubscribe();
    }
  }
}
