import {Event} from 'app/app';

/**
 * An event fired when idoc context is created.
 */
@Event()
export class AfterIdocLoadedEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}
