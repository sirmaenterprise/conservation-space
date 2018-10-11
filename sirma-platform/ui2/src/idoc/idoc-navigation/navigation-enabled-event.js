import {Event} from 'app/app';

/**
 * Event fired when navigation section in document is opened.
 */
@Event()
export class NavigationEnabledEvent {
  constructor(id) {
    this.data = {id};
  }

  getData() {
    return this.data;
  }
}
