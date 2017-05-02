import {Event} from 'app/app';

/**
 * Fired when a comment is expanded and the others should be collapsed.
 */
@Event()
export class CommentExpandedEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }

}