import {Inject, Injectable} from 'app/app';
import {RestClient} from 'services/rest-client';
import {Eventbus} from 'services/eventbus/eventbus';
import {RouterStateChangeStartEvent} from 'common/router/router-state-change-start-event';
import {RequestsCacheService} from 'services/rest/requests-cache-service';
import _ from 'lodash';

const serviceUrl = '/codelist';
const MANAGEMENT_URL = '/codelists';

export const EXPORT_SERVICE_URL = MANAGEMENT_URL + '/export';
export const UPDATE_SERVICE_URL = MANAGEMENT_URL + '/upload';
export const OVERWRITE_SERVICE_URL = MANAGEMENT_URL + '/upload/overwrite';

@Injectable()
@Inject(RestClient, Eventbus, RequestsCacheService)
export class CodelistRestService {

  constructor(restClient, eventbus, requestsCacheService) {
    this.restClient = restClient;
    this.cache = {};
    eventbus.subscribe(RouterStateChangeStartEvent, () => {
      this.cache = {};
    });
    this.requestsMap = new Map();
    this.requestsCacheService = requestsCacheService;
  }

  getCodelist(opts) {
    let url = serviceUrl + '/' + opts.codelistNumber;

    let config = {
      params: {
        'customFilters[]': opts.customFilters,
        'filterBy': opts.filterBy,
        'inclusive': opts.inclusive,
        'filterSource': opts.filterSource,
        'q': opts.q
      }
    };

    if (!CodelistRestService.hasAdditionalOptions(opts)) {
      if (!this.cache[opts.codelistNumber]) {
        this.cache[opts.codelistNumber] = this.restClient.get(url, config);
      }
      return this.cache[opts.codelistNumber];
    }
    return this.requestsCacheService.cache(url, config, this.requestsMap, () => {
      return this.restClient.get(url, config);
    });
  }

  getCodeLists() {
    return this.restClient.get(MANAGEMENT_URL);
  }

  saveCodeList(codeList) {
    return this.restClient.post(`${MANAGEMENT_URL}/${codeList.value}`, codeList);
  }

  exportCodeLists() {
    let config = {
      responseType: 'arraybuffer',
      headers: {
        'Accept': 'application/octet-stream'
      }
    };
    return this.restClient.get(EXPORT_SERVICE_URL, config);
  }

  /**
   * Service URL for adding set of code lists to the existing ones in the system.
   */
  getUpdateServiceUrl() {
    return this.restClient.getUrl(UPDATE_SERVICE_URL);
  }

  /**
   * Service URL for overwriting all code lists in the system with another set.
   */
  getOverwriteServiceUrl() {
    return this.restClient.getUrl(OVERWRITE_SERVICE_URL);
  }

  /**
   * Check if opts object has not empty values for any additional options besides codelistNumber
   * @param opts
   * @returns {boolean} true if there are any additional options, false otherwise
   */
  static hasAdditionalOptions(opts) {
    return !!_.find(Object.keys(opts), (key) => {
      return key !== 'codelistNumber' && opts[key];
    });
  }
}
