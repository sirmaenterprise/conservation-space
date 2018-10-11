import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';
import {serviceUrl as INSTANCE_SERVICE_URL} from 'services/rest/instance-service';

import _ from 'lodash';

const SERVICE_URL = `${INSTANCE_SERVICE_URL}/content/share`;

@Injectable()
@Inject(RestClient)
export class InstanceShareRestService {

  constructor(restClient) {
    this.restClient = restClient;
    this.config = {};
    this.config.headers = {
      'Accept': HEADER_V2_JSON,
      'Content-Type': HEADER_V2_JSON
    };
  }

  /**
   * Sends instance id's and returns download links for requested instances. Returned links can't be used immediately as
   * is. The service schedules delayed tasks for instance export in backend. These tasks should be triggered by calling
   * the POST method on the same rest endpoint. After the export tasks are triggered and export completes (it's
   * asynchronous) then the links becomes usable and would return the exported instance.
   */
  shareLinks(params) {
    let config = _.cloneDeep(this.config);
    config.params = params;
    config.params.contentFormat = 'word';
    return this.restClient.get(SERVICE_URL, config);
  }

  /**
   * Initiates instance export and publish of instance links.
   */
  triggerShare(sharecodes) {
    this.restClient.post(SERVICE_URL, sharecodes, this.config);
  }
}