import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';

const SERVICE_URL = '/theme';

@Injectable()
@Inject(RestClient)
export class ThemeRestClient {

  constructor(restClient) {
    this.restClient = restClient;
    this.config = {
      headers: {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      }
    };
  }

  getThemeData() {
    return this.restClient.get(SERVICE_URL);
  }
}
