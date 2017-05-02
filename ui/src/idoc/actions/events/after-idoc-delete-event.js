import {Event} from 'app/app';

/**
 * An event fired after idoc has been deleted.
 * The payload contains:
 * - object id
 */
@Event()
export class AfterIdocDeleteEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}