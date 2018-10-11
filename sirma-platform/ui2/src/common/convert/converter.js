/**
 * A convenient interface for converter implementation classes.
 */
export class Converter {

  constructor() {
    if (typeof this.convert !== 'function') {
      throw new TypeError('A converter must provide a \'convert\' function!');
    }
  }

}