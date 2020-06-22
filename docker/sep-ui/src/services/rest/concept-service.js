import {Inject, Injectable} from 'app/app';
import {RestClient} from 'services/rest-client';
import {RequestsCacheService} from 'services/rest/requests-cache-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {RouterStateChangeStartEvent} from 'common/router/router-state-change-start-event';

const SERVICE_BASE_URL = '/concept';

@Injectable()
@Inject(RestClient, RequestsCacheService, Eventbus)
export class ConceptService {

  constructor(restClient, requestsCacheService, eventbus) {
    this.restClient = restClient;
    this.requestsCacheService = requestsCacheService;
    this.requestsMap = new Map();

    this.cache = {};
    eventbus.subscribe(RouterStateChangeStartEvent, () => {
      this.cache = {};
    });
  }

  getConceptHierarchy(scheme, broader) {
    let params = {
      scheme,
      broader
    };

    let cacheKey = scheme + broader;

    if (!this.cache[cacheKey]) {
      this.cache[cacheKey] = this.requestsCacheService.cache(`${SERVICE_BASE_URL}`, params, this.requestsMap, () => {
        return this.restClient.get(`${SERVICE_BASE_URL}`,  {params});
      }).then(response => {
        return response.data;
      });
    }

    return this.cache[cacheKey];
  }

}
