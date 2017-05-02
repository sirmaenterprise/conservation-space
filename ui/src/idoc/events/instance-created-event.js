import {Event} from 'app/app';

/**
 * An event fired when an instance is created.
 */
@Event()
export class InstanceCreatedEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}