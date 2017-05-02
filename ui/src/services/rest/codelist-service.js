import {Inject, Injectable} from 'app/app';
import {RestClient} from 'services/rest-client';

const serviceUrl = '/codelist';

@Injectable()
@Inject(RestClient)
export class CodelistRestService {

  constructor(restClient) {
    this.restClient = restClient;
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
    return this.restClient.get(url, config);
  }
}
