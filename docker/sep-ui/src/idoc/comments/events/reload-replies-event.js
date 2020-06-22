import {Event} from 'app/app';

/**
 * Fired when new reply is added.
 */
@Event()
export class ReloadRepliesEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}
