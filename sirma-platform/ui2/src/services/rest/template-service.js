import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';

const URL = '/templates';

@Injectable()
@Inject(RestClient)
export class TemplateService {

  constructor(restClient) {
    this.restClient = restClient;

    this.config = {
      headers: {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      }
    };
  }

  create(templateData) {
    let payload = {
      forType: templateData.forType,
      title: templateData.title,
      purpose: templateData.purpose,
      primary: templateData.primary,
      sourceInstance: templateData.sourceInstance
    };

    return this.restClient.post(URL, payload, this.config);
  }

  loadTemplates(definitions, purpose, filterCriteria) {
    // TODO feature toggle. Remove the call to the old (get endpoint) service when the new one gets stable
    if (purpose || filterCriteria) {
      let payload = {
        group: definitions,
        purpose,
        filter: filterCriteria
      };

      return this.restClient.post(URL + '/search', payload, this.config);
    }

    let groups = definitions;
    if (typeof definitions === 'string') {
      groups = [definitions];
    }

    let qparams = '';
    if (groups && groups.length) {
      qparams += '?group-id=' + groups.join('&group-id=');
    }
    return this.restClient.get(URL + qparams, this.config);
  }

  loadContent(id) {
    return this.restClient.get(`${URL}/${id}/content`, {headers: {'Accept': 'text/html'}});
  }

  setTemplateAsPrimary(templateInstanceId) {
    return this.restClient.post(`/instances/${templateInstanceId}/actions/set-template-as-primary`, {}, this.config);
  }

  editTemplateRules(templateInstanceId, rules) {
    // null value will guarantee that the property is sent to the server and won't have any value
    if (!rules) {
      rules = null;
    }

    let payload = {
      rule: rules
    };

    return this.restClient.post(`/instances/${templateInstanceId}/actions/edit-template-rule`, payload, this.config);
  }

  deactivateTemplate(templateInstanceId) {
    return this.restClient.post(`/instances/${templateInstanceId}/actions/deactivate-template`, {}, this.config);
  }

  updateInstanceTemplate(templateInstanceId) {
    let payload = {
      templateInstance: templateInstanceId
    };
    return this.restClient.post('/instances/actions/update-instance-template', payload, this.config);
  }

  updateSingleInstanceTemplate(instanceId) {
    let payload = {
      instance: instanceId
    };
    return this.restClient.post('/instances/actions/update-to-latest-template', payload, this.config);
  }

  getActualTemplateVersion(instanceId) {
    let payload = {
      instance: instanceId
    };
    return this.restClient.post('/instances/template-version', payload, this.config);
  }

}
