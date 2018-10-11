import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';

const RELATIONS_URL = '/relations';

@Injectable()
@Inject(RestClient)
export class RelationsService {

  constructor(client) {
    this.restClient = client;

    this.config = {
      headers: {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      }
    };
  }

  suggest(definitionId, propertyName, keywords, properties) {
    this.config.params = {
      properties: properties
    };
    return this.restClient.post(RELATIONS_URL + '/suggest', {
      "definitionId": definitionId,
      "propertyName": propertyName,
      "keywords": keywords
    }, this.config);
  };
}
