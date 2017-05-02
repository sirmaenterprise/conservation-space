import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';

const URL = '/templates';

@Injectable()
@Inject(RestClient)
export class TemplateService {

  constructor(restClient) {
    this.restClient = restClient;
  }

  create(templateData) {
    var payload = {
      forType: templateData.forType,
      properties : {
        title : templateData.title,
        purpose : templateData.purpose,
        primary : templateData.primary
      },
      "sourceInstance" : templateData.sourceInstance
    };

    return this.restClient.post(URL, payload, {
      headers: {
      'Accept': HEADER_V2_JSON,
      'Content-Type': HEADER_V2_JSON,
      }
    });
  }

  loadTemplates(definitions) {
    var groups = definitions;
    if (typeof definitions === 'string') {
      groups = [ definitions ];
    }

    var qparams = '';
    if (groups && groups.length) {
      qparams += '?group-id=' + groups.join('&group-id=');
    }
    return this.restClient.get(URL + qparams, { headers: { 'Accept': HEADER_V2_JSON } });
  }

  loadContent(id) {
    return this.restClient.get(`${URL}/${id}/content`, { headers: { 'Accept': 'text/html' } });
  }
}