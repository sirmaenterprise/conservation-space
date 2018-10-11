import {Inject, Injectable} from 'app/app';
import {RequestsCacheService} from 'services/rest/requests-cache-service';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';
import _ from 'lodash';

const serviceUrl = '/definition';

// Part of the endpoints are migrated to REST API v2
const serviceUrlV2 = '/definitions';

@Injectable()
@Inject(RestClient, RequestsCacheService)
export class DefinitionService {
  constructor(restClient, requestsCacheService) {
    this.restClient = restClient;
    this.config = {};
    this.config.headers = {
      'Accept': HEADER_V2_JSON,
      'Content-Type': HEADER_V2_JSON
    };
    this.requestsMap = new Map();
    this.requestsCacher = requestsCacheService;
  }

  /**
   * Uses old REST service to load definitions. New one can't load models for semantic classes.
   * If loaded, they are reused.
   * @param identifiers
   * @returns {*}
   */
  getFields(identifiers) {
    let url = serviceUrl + '/fields';
    let data = {
      identifiers: identifiers
    };
    return this.requestsCacher.cache(url, identifiers, this.requestsMap, () => {
      return this.restClient.post(url, data);
    });
  }

  getDefinitions(identifiers) {
    let config = _.cloneDeep(this.config);
    config.params = {
      id: identifiers
    };
    return this.requestsCacher.cache(serviceUrlV2, identifiers, this.requestsMap, () => {
      return this.restClient.get(serviceUrlV2, config);
    });
  }
}
