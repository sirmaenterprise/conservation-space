import {Event} from 'app/app';

/**
 * Event fired when comments filter is triggered
 */
@Event()
export class CommentsFilteredEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}
