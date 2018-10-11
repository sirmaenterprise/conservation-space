import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';
const URL = '/integration';

@Injectable()
@Inject(RestClient)
export class ExternalObjectService {

  constructor(restClient) {
    this.restClient = restClient;
    this.config = {};
    this.config.headers = {
      'Accept': HEADER_V2_JSON,
      'Content-Type': HEADER_V2_JSON
    };
  }

  importObjects(objects) {
    return this.restClient.post(URL + '/import', objects, this.config);
  }
}
