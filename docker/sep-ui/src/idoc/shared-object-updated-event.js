import {Event} from 'app/app';

/**
 * Event fired when a shared object in the context has been updated.
 *
 * data:
 * {
 *   widgetId: widgetId, // The widget for which this object is registered
 *   objectId: objectId  // The object id for which this event is fired
 * }
 *
 */
@Event()
export class SharedObjectUpdatedEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}
