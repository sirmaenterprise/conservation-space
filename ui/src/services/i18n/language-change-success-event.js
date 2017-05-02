import {Event} from 'app/app';

/**
 * An event fired after a successful language change
 */
@Event()
export class LanguageChangeSuccessEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}
