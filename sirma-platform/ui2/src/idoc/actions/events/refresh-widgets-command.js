import {Event} from 'app/app';

/**
 * An command used to refresh widgets.
 */
@Event()
export class RefreshWidgetsCommand {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}