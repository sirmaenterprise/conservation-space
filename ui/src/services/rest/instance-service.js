import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';
import {SearchResponse} from './response/search-response';
import _ from 'lodash';

const serviceUrl = '/instances';

@Injectable()
@Inject(RestClient)
export class InstanceRestService {
  constructor(restClient) {
    this.restClient = restClient;
    this.config = {};
    this.config.headers = {
      'Accept': HEADER_V2_JSON,
      'Content-Type': HEADER_V2_JSON
    };
  }

  create(data, config) {
    config = _.defaults(config || {}, this.config);
    return this.restClient.post(serviceUrl, data, config);
  }

  update(id, data) {
    return this.restClient.patch(`${serviceUrl}/${id}`, data, this.config);
  }

  updateAll(data) {
    return this.restClient.patch(serviceUrl, data, this.config);
  }

  load(id, config) {
    config = _.defaults(config || {}, this.config);
    return this.restClient.get(`${serviceUrl}/${id}`, config);
  }

  loadDefaults(definitionId, parentInstanceId) {
    var config = _.cloneDeep(this.config);
    config.params = {
      'definition-id': definitionId,
      'parent-instance-id': parentInstanceId
    };
    return this.restClient.get(`${serviceUrl}/defaults`, config);
  }

  loadView(id, config) {
    config = _.defaults(config || {}, this.config);
    return this.restClient.get(`${serviceUrl}/${id}/view`, config);
  }

  loadContextPath(id, config) {
    config = _.defaults(config || {}, this.config);
    return this.restClient.get(`${serviceUrl}/${id}/context`, config);
  }

  loadBatch(ids, config) {
    config = _.defaults(config || {}, this.config);
    return this.restClient.post(`${serviceUrl}/batch`, ids, config);
  }

  deleteInstance(id) {
    return this.restClient.deleteResource(`${serviceUrl}/${id}`, this.config);
  }

  getAllowedActions(id, type) {
    return this.restClient.get(`${serviceUrl}/${id}/actions?instanceType=${type}`);
  }

  getAllowedChildren(id, type, childType) {
    return this.restClient.get(`${serviceUrl}/${id}/allowedChildren?instanceType=&childType=${childType}`, {
      'Content-Type': 'application/json'
    });
  }

  preview(id) {
    var config = _.cloneDeep(this.config);
    config.responseType = 'arraybuffer';
    return this.restClient.get(`${serviceUrl}/${id}/content/preview`, config);
  }

  loadModel(id, operation, config) {
    config = _.defaults(config || {}, this.config);
    config.params = {
      operation
    };
    return this.restClient.get(`${serviceUrl}/${id}/model`, config);
  }

  loadModels(ids, operation, config) {
    config = _.defaults(config || {}, this.config);
    config.params = _.defaults(config.params || {}, {operation: operation});
    return this.restClient.post(`${serviceUrl}/model/batch`, ids, config);
  }

  getContentUploadUrl(instanceId) {
    return this.restClient.getUrl(`${serviceUrl}/${instanceId}/content/`);
  }

  createDraft(id, content) {
    return this.restClient.post(`${serviceUrl}/${id}/drafts`, content, this.config);
  }

  loadDraft(id) {
    return this.restClient.get(`${serviceUrl}/${id}/drafts`, this.config);
  }

  deleteDraft(id) {
    return this.restClient.deleteResource(`${serviceUrl}/${id}/drafts`, this.config);
  }

  getVersions(id, offset, limit) {
    var config = _.cloneDeep(this.config);
    config.params = {
      offset,
      limit
    };
    return this.restClient.get(`${serviceUrl}/${id}/versions`, config);
  }

  loadHistory(id, limit, offset) {
    return this.loadAuditDataForInstances([id], limit, offset);
  }

  loadAuditDataForInstances(identifiers, limit, offset, dateRange) {
    let data = {
      instanceIds: identifiers,
      limit: parseInt(limit),
      offset: offset,
      dateRange: dateRange
    };

    return this.restClient.post(`${serviceUrl}/history/batch`, data, this.config).then((response) => {
      return new SearchResponse(response);
    });
  }

  getTooltip(id, config) {
    config = _.defaults(config || {}, this.config);
    return this.restClient.get(`${serviceUrl}/${id}/tooltip`, config);
  }

  /**
   * Based on the given instance id, the service will return properties that prepared for cloning. The properties are
   * copy of the original instance's properties.
   *
   * @param id - the instance id for which properties will be prepared
   * @returns Promise which resolves to the prepared properties
   */
  cloneProperties(id) {
    return this.restClient.get(`${serviceUrl}/${id}/actions/clone`, this.config);
  }

  /**
   * Performs cloning of an instance with the given cloned payload and original instance id. The provided clone
   * payload should contain:
   * {
   *  id: '...', // UUID of the cloned instance
   *  parentId: '...', // the context in which the instance will be cloned as a child
   *  definitionId: '...', // the instance definition id
   *  properties: {...} // the cloned instance properties with which the new instance will be created
   * }
   *
   * @param id - the original instance id which will be cloned
   * @param data - object containing cloned payload
   *
   */
  cloneInstance(id, data) {
    return this.restClient.post(`${serviceUrl}/${id}/actions/clone`, data, this.config);
  }
}
