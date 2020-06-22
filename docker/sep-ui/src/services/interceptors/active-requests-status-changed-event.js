import {Event} from 'app/app';

@Event()
export class ActiveRequestsStatusChangedEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}