import {Event} from 'app/app';

/**
 * An event fired when personal mailbox is updated.
 */
@Event()
export class PersonalMailboxUpdatedEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}