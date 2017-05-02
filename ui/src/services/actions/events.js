import {Event} from 'app/app';

/**
 * Fired when an action is executed on an object
 */
@Event()
export class ActionExecutedEvent {
  constructor(action, context, response) {
    this.data = {
      action,
      context,
      response
    };
  }

  getData() {
    return this.data;
  }
}
