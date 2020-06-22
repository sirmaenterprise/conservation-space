import {Inject, Injectable} from 'app/app';
import {RestClient} from 'services/rest-client';

const SERVICE_BASE_URL = '/configurations/';
const TENANT_CONFIGURATION = SERVICE_BASE_URL + 'tenant';
const SERVICE_RELOAD = SERVICE_BASE_URL + 'reload';

/**
 * Responsible for access and update configurations.
 *
 * @author svelikov
 */
@Injectable()
@Inject(RestClient)
export class ConfigurationRestService {

  constructor(restClient) {
    this.restClient = restClient;
  }

  /**
   * Loads the available tenant configurations. This could return different results depending if the currently
   * logged user is an administrator or not.
   *
   * @returns Promise resolved with array of the configurations as data. Example: <code>{ data: [...] }</code>
   */
  loadConfigurations() {
    return this.restClient.get(TENANT_CONFIGURATION, {
      q: 'ui|application'
    });
  }

  /**
   * Updates the server with the provided configurations. The update requires only key & value to be present.
   *
   * @param configurations - array of configurations to update.
   * @returns Promise resolved with array of the configurations as data. Example: <code>{ data: [...] }</code>
   */
  updateConfigurations(configurations) {
    return this.restClient.post(TENANT_CONFIGURATION, configurations);
  }

  /**
   * Reloads the server configurations.
   *
   * @returns a promise resolved when the configurations are reloaded
   */
  reloadConfigurations() {
    return this.restClient.get(SERVICE_RELOAD);
  }

}
