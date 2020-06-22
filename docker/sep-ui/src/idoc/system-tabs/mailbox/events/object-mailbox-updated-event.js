import {Event} from 'app/app';

/**
 * An event fired when an object mailbox tab is updated after execution of user action.
 */
@Event()
export class ObjectMailboxUpdatedEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}