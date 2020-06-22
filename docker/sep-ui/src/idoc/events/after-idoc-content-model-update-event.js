import {Event} from 'app/app';

/**
 * An event fired just before saving an iDoc
 */
@Event()
export class AfterIdocContentModelUpdateEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}
