import {Inject, Injectable} from 'app/app';
import {RestClient} from 'services/rest-client';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {SearchService} from 'services/rest/search-service';
import {SearchResolverService} from 'services/resolver/search-resolver-service';
import {RequestsCacheService} from 'services/rest/requests-cache-service';

export const AUDIT_LOG_URL = '/events/search';

@Injectable()
@Inject(RestClient, SearchResolverService, PromiseAdapter, RequestsCacheService)
export class AuditLogService extends SearchService {

  constructor(restClient, searchResolverService, promiseAdapter, requestsCacheService) {
    super(restClient, searchResolverService, promiseAdapter, requestsCacheService);
  }

  /**
   * This overrides the service URL in {@link SearchService} when performing requests.
   */
  getServiceUrl() {
    return AUDIT_LOG_URL;
  }

}