import {Injectable} from 'app/app';

/**
 * Configuration service stub.
 *
 * @author svelikov
 */
@Injectable()
export class Configuration {

  constructor(configs) {
    this.config = configs;
  }

  get(key) {
    return this.config[key];
  }

}

Configuration.APPLICATION_MODE_DEVELOPMENT = 'application.mode.development';