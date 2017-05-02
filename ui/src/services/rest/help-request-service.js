import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';

const serviceUrl = '/user/help/request';

@Injectable()
@Inject(RestClient)
export class HelpRequestRestService {

  constructor(restClient) {
    this.restClient = restClient;
    this.config = {};
    this.config.headers = {
      'Accept': HEADER_V2_JSON,
      'Content-Type': HEADER_V2_JSON
    };
  }

  /**
   * Send request to server.
   * @param data - json object which will be send to server.
   * Example of json:
   * {
   *  subject:      "Subject entered form user",
   *  type:         "Type of help request value from CL600",
   *  description:  "Content of email server entered from user"
   * }
   *
   * @returns server response.
   */
  sendHelpRequest(data) {
    let requestData = {
         params: data
    };

    return this.restClient.post(serviceUrl, requestData, this.config);
  }
}