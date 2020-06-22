import {Event} from 'app/app';

/**
 * An event fired after idoc actions execution is completed allowing some after event tasks to be executed - like data
 * reloading or clearing.
 * As event payload are provided the action definition and the server response.
 */
@Event()
export class InstanceRefreshEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}