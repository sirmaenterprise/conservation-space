import {Configurable} from 'components/configurable';

/**
 * Abstract class for creating reusable components which wrap external libraries.
 */
export class ReusableComponent extends Configurable {
  constructor(defaultConfig) {
    super(defaultConfig);

    if (typeof this.createActualConfig !== 'function') {
      throw new TypeError('Must override createActualConfig function');
    }

    this.createActualConfig();
    if (this.actualConfig === undefined) {
      throw new TypeError('createActualConfig must create actualConfig configuration object for the underlying library');
    }
  }
}
