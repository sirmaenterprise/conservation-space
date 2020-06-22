/**
 * Abstract class for every action filter handler.
 * Created by tdossev on 20.10.2016 г.
 */

export class Filter {
  constructor() {
    if (typeof this.filter !== 'function') {
      throw new TypeError('Filter handlers must override the \'filter\' function!');
    }

    if (typeof this.canHandle !== 'function') {
      throw new TypeError('Filter handlers must override the \'canHandle\' function!');
    }
  }
}