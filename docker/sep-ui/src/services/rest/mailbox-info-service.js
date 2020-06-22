import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';
import _ from 'lodash';

const serviceUrl = '/mailbox';

@Injectable()
@Inject(RestClient)
export class MailboxInfoService {

  constructor(restClient) {
    this.restClient = restClient;
    this.config = {};
    this.config.headers = {
      'Accept': HEADER_V2_JSON,
      'Content-Type': HEADER_V2_JSON
    };
  }

  /**
   * Get unread messages count for all registered accounts
   * @param accountName - mailbox account
   * @param config - service configuration
   * @returns number of unread messages
   */
  getUnreadMessagesCount(accountName, config) {
    config = _.defaults(config || {}, this.config);
    return this.restClient.get(`${serviceUrl}/${accountName}/unread`, config);
  }

}
