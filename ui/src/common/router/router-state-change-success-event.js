import {Event} from 'app/app';

/**
 * An event fired after the router finishes a state change.
 *
 * @author svelikov
 */
@Event()
// istanbul ignore next
export class RouterStateChangeSuccessEvent {

  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}