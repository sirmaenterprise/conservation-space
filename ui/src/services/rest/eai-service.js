import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';
import _ from 'lodash';

const INTEGRATION_URL = '/integration';

/**
 * Service providing information about the external systems and their related models & properties.
 *
 * @author Mihail Radkov
 */
@Injectable()
@Inject(RestClient)
export class EAIService {

  constructor(restClient) {
    this.restClient = restClient;

    this.config = {
      headers: {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      }
    };
  }

  getModels(system) {
    return this.restClient.get(`${INTEGRATION_URL}/${system}/model/search/types`, this.config);
  }

  getProperties(system, type) {
    var encodedType = encodeURIComponent(type);
    return this.restClient.get(`${INTEGRATION_URL}/${system}/model/search/${encodedType}/properties`, this.config).then((response) => {
      return this.convertResponse(response);
    });
  }

  convertResponse(response) {
    if (response && response.data) {
      response.data.forEach((property) => {
        if (property.type === 'datetime') {
          // Until this is standardized to be either datetime or dateTime across services/models we will need this conversion.
          property.type = 'dateTime';
        } else if (property.type === 'text' || property.type === 'any') {
          // Same as above
          property.type = 'string';
        }

        if (property.type === 'string') {
          property.singleValued = true;
        }
      });
    }
    return response;
  }

  getRegisteredSystems() {
    return this.restClient.get(`${INTEGRATION_URL}`, this.config);
  }
}
