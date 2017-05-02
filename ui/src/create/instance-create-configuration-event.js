import {Event} from 'app/app';

/**
 * An event fired when the instance create configuration component loads all base types after context manipulation
 */
@Event()
export class InstanceCreateConfigurationEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}