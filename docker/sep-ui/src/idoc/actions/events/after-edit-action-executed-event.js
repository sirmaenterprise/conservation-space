import {Event} from 'app/app';

/**
 * An event fired after edit idoc action is executed.
 */
@Event()
export class AfterEditActionExecutedEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}
