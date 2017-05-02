import {Event} from 'app/app';

/**
 * An event fired before obtaining idoc content
 */
@Event()
export class IdocContentModelUpdateEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}
