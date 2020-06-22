import {Event} from 'app/app';

/**
 * An event fired when idoc content is sanitized
 */
@Event()
export class IdocContentSanitizedEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}
