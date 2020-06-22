import {Event} from 'app/app';

/**
 * An event fired after saving an iDoc
 */
@Event()
export class AfterIdocSaveEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}
