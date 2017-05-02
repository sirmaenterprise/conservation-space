import {Event} from 'app/app';

/**
 * An event fired before the router to init a new state.
 *
 * @author svelikov
 */
@Event()
// istanbul ignore next
export class RouterStateChangeStartEvent {

  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }

}