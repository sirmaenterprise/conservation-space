import {Inject, Injectable} from 'app/app';
import {RestClient} from 'services/rest-client';

const serviceUrl = '/logger';

/**
 * Rest client that provides methods for invoking the logging rest service.
 *
 * @author svelikov
 */
@Injectable()
@Inject(RestClient)
export class LoggingRestService {

  constructor(restClient) {
    this.restClient = restClient;
  }

  logMessage(message) {
    return this.restClient.post(serviceUrl, message);
  }

}
