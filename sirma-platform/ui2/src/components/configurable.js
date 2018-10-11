import _ from 'lodash';

export class Configurable {
  constructor(defaultConfig) {
    if (defaultConfig === undefined) {
      throw new TypeError("Default configuration object is expected as first argument");
    }

    this.config = this.config || {};

    _.defaultsDeep(this.config, defaultConfig);
  }
}
