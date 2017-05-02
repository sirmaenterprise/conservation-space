import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';

const SERVICE_PATH = '/libraries';

@Injectable()
@Inject(RestClient)
export class LibrariesService {

  constructor(restClient) {
    this.restClient = restClient;
  }

  loadLibraries() {
    return this.restClient.get(SERVICE_PATH, {
      headers: {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      }
    });
  }
}
