import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';
import {SearchResponse} from './response/search-response';
import {InstanceObject} from 'models/instance-object';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {RequestsCacheService} from 'services/rest/requests-cache-service';
import {InstanceResponse} from './response/instance-response';
import {ObjectPropertiesResponse} from './response/object-properties-response';
import {BatchInstanceResponse} from './response/batch-instance-response';

import _ from 'lodash';

export const serviceUrl = '/instances';
export const EDIT_OPERATION_NAME = 'editDetails';

@Injectable()
@Inject(RestClient, PromiseAdapter, RequestsCacheService)
export class InstanceRestService {

  constructor(restClient, promiseAdapter, requestsCacheService) {
    this.restClient = restClient;
    this.promiseAdapter = promiseAdapter;
    this.requestsMap = new Map();
    this.requestsCacheService = requestsCacheService;
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

  /**
   * Loads the instance model.
   * @return InstanceResponse
   */
  load(id, config) {
    config = _.defaults(config || {}, this.config);
    return this.restClient.get(`${serviceUrl}/${id}`, config).then((response) => {
      return new InstanceResponse(response);
    });
  }

  loadDefaults(definitionId, parentInstanceId) {
    let config = _.cloneDeep(this.config);
    config.params = {
      'definition-id': definitionId,
      'parent-instance-id': parentInstanceId
    };
    return this.requestsCacheService.cache(`${serviceUrl}/defaults`, [definitionId, parentInstanceId], this.requestsMap, () => {
      return this.restClient.get(`${serviceUrl}/defaults`, config).then((response) => {
        // This service is used only when new instance is going to be created. As the model don't give
        // any information if the instance is new or is persisted, a separated flag is added.
        response.data.isNewInstance = true;
        return response;
      });
    });
  }

  loadView(id, config) {
    config = _.defaults(config || {}, this.config);
    return this.restClient.get(`${serviceUrl}/${id}/view`, config);
  }

  loadContextPath(id, config) {
    config = _.defaults(config || {}, this.config);
    return this.restClient.get(`${serviceUrl}/${id}/context`, config);
  }

  /**
   * Retrieves instance models in JSON format. The method will return the models for all of the found instances(ids)
   * that are requested. Config argument could be used to pass, if deleted object should be loaded or not, by default
   * they won't be loaded. Also through the config might be passed specific properties that should be loaded for the
   * instances, if not all of them are required. With the passed ids and the config parameters will be build specific
   * payload for the service request.
   *
   * @param ids of the instances, which model should be loaded
   * @param config might be used to specify properties that should be returned for the instances, if not all of them are
   *               required. Also could be used to specify, if deleted instances should be returned as well
   * @returns BatchInstanceResponse which holds array of instance models for the requested ids
   */
  loadBatch(ids, config) {
    let localConfig = _.cloneDeep(config || {});
    localConfig = _.defaults(localConfig, this.config);
    let payload = {
      instanceIds: ids,
      properties: [],
      allowDeleted: false
    };

    if (localConfig.params) {
      if (localConfig.params.deleted) {
        payload.allowDeleted = localConfig.params.deleted;
        delete localConfig.params.deleted;
      }

      if (localConfig.params.properties) {
        payload.properties = localConfig.params.properties;
        delete localConfig.params.properties;
      }
    }

    return this.restClient.post(`${serviceUrl}/batch`, payload, localConfig).then((response) => {
      return new BatchInstanceResponse(response);
    });
  }

  loadBatchDeleted(ids) {
    let batchLoadConfig = {
      params: {
        deleted: true
      }
    };
    return this.loadBatch(ids, batchLoadConfig);
  }

  deleteInstance(id) {
    return this.restClient.deleteResource(`${serviceUrl}/${id}`, this.config);
  }

  /**
   * Loads object property value. The value contains an array with ids of all the relations that the object has under
   * that relation type.
   *
   * @param id The object id
   * @param property The object property name (the relation type)
   * @param offset
   * @param limit
   * @returns ObjectPropertiesResponse
   */
  getInstanceProperty(id, property, offset, limit) {
    return this.restClient.get(`${serviceUrl}/${id}/object-properties?propertyName=${property}&offset=${offset}&limit=${limit}`).then((response) => {
      return new ObjectPropertiesResponse(response);
    });
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

  /**
   * Retrieves instance definition models in JSON format. The method will return the models for all of the requested
   * instances(ids), if such is found. The operation argument is used for mandatory fields calculation. From the input
   * arguments will be build specific payload object for the service request.
   *
   * @param ids of the instances which definition models should be returned
   * @param operation that is executed. Used for mandatory fields calculation
   * @param config might be used to specify properties that should be returned, if not all of them are needed. Then the
   *               requested models will contain only this properties, plus any dependent properties(used in conditions
   *               to calculate, if specific property is mandatory or not)
   * @returns {*} map of instance ids as key and its corresponding definition model as value
   */
  loadModels(ids, operation, config) {
    let localConfig = _.cloneDeep(config || {});
    localConfig = _.defaults(localConfig, this.config);
    let payload = {
      instanceIds: ids,
      operation,
      requestedFields: []
    };

    if (localConfig.params && localConfig.params.properties) {
      payload.requestedFields = localConfig.params.properties;
      delete localConfig.params.properties;
    }

    return this.restClient.post(`${serviceUrl}/model/batch`, payload, localConfig);
  }

  getContentUploadUrl(instanceId) {
    return this.restClient.getUrl(`${serviceUrl}/${instanceId}/content/`);
  }

  getRevisionUploadUrl(instanceId) {
    return this.restClient.getUrl(`${serviceUrl}/${instanceId}/revision/`);
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
    let config = _.cloneDeep(this.config);
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
      offset,
      dateRange
    };

    return this.restClient.post(`${serviceUrl}/history/batch`, data, this.config).then((response) => {
      return new SearchResponse(response);
    });
  }

  getTooltip(id, config) {
    config = _.defaults(config || {}, this.config);
    return this.restClient.get(`${serviceUrl}/${id}/tooltip`, config);
  }

  compareVersions(id, firstVersionId, secondVersionId) {
    let data = {
      userOperation: 'compareVersions',
      firstSourceId: firstVersionId,
      secondSourceId: secondVersionId
    };
    return this.restClient.post(`${serviceUrl}/${id}/actions/compare-versions`, data, this.config);
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

  getContentPreviewUrl(id, contentType) {
    return this.restClient.getUrl(`${serviceUrl}/${id}/content/preview?purpose=${contentType}`);
  }

  getContentDownloadUrl(id) {
    return this.restClient.getUrl(`${serviceUrl}/${id}/content?download=true`);
  }

  /**
   * Loads instance and definition models, then creates and returns an InstanceObject using both.
   *
   * @param instanceId
   * @param operation
   * @return {*}
   */
  loadInstanceObject(instanceId, operation) {
    let instanceLoader = this.load(instanceId);
    let instanceDefinitionLoader = this.loadModel(instanceId, operation);

    return this.promiseAdapter.all([instanceLoader, instanceDefinitionLoader]).then(([{['data']: instance}, {['data']: definition}]) => {
      definition.headers = instance.headers;
      let instanceObject = new InstanceObject(instanceId, definition, undefined);
      instanceObject.mergePropertiesIntoModel(instance.properties);
      return instanceObject;
    });
  }
}