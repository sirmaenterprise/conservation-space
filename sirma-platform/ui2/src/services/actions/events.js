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

/**
 * Fired when an action is failed and can't be completed or the user cancels the execution.
 */
@Event()
export class ActionInterruptedEvent {
  constructor(action) {
    this.data = {
      action
    };
  }

  getData() {
    return this.data;
  }
}
