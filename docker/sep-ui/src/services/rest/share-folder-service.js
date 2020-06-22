import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';
import _ from 'lodash';

const serviceUrl = '/mailbox';
/**
 * Mailbox share folder service used to mount tenant share folder to object mailboxes.
 *
 * @author g.tsankov
 */
@Injectable()
@Inject(RestClient)
export class ShareFolderService {

  constructor(restClient) {
    this.restClient = restClient;
    this.config = {};
    this.config.headers = {
      'Accept': HEADER_V2_JSON,
      'Content-Type': HEADER_V2_JSON
    };
  }

  mountObjectShareFolder(accountName, config) {
    config = _.defaults(config || {}, this.config);
    return this.restClient.post(`${serviceUrl}/${accountName}/mount`, config);
  }

}