import {Event} from 'app/app';

/**
 * Event triggered when the comment content dialog cancel button is pressed.
 */
@Event()
export class CommentContentDialogClosedEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}
