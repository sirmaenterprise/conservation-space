import {Event} from 'app/app';

/**
 * An event fired after form validation.
 */
@Event()
export class AfterFormValidationEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}
