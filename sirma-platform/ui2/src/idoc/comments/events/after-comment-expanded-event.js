import {Event} from 'app/app';

/**
 * Fired when a comment is expanded and the necessary data is loaded.
 */
@Event()
export class AfterCommentExpandedEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }

}