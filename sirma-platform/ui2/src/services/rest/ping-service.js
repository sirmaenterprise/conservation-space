import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';

export const SERVICE_URL = '/ping';

@Injectable()
@Inject(RestClient)
export class PingService {

  constructor(restClient) {
    this.restClient = restClient;
    this.config = {
      headers: {
        'Accept': HEADER_V2_JSON
      }
    };
  }

  ping() {
    return this.restClient.get(SERVICE_URL);
  }
}