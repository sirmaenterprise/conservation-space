import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';
import _ from 'lodash';

const serviceUrl = '/definition';

// Part of the endpoints are migrated to REST API v2
const serviceUrlV2 = '/definitions';

@Injectable()
@Inject(RestClient)
export class DefinitionService {
  constructor(restClient) {
    this.restClient = restClient;
    this.config = {};
    this.config.headers = {
      'Accept': HEADER_V2_JSON,
      'Content-Type': HEADER_V2_JSON
    };
  }

  /**
   * Uses old REST service to load definitions. New one can't load models for semantic classes
   * @param identifiers
   * @returns {*}
   */
  getFields(identifiers) {
    let url = serviceUrl + '/fields';
    let data = {
      identifiers: identifiers
    };
    return this.restClient.post(url, data);
  }

  getTypes(options) {
    let config = _.cloneDeep(this.config);
    config.params = options;
    return this.restClient.get(serviceUrlV2 + '/types', config);
  }

  getDefinitions(identifiers) {
    let config = _.cloneDeep(this.config);
    config.params = {
      id: identifiers
    };
    return this.restClient.get(serviceUrlV2, config);
  }
}
