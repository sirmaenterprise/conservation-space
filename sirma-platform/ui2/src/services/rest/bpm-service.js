import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';
import {ModelUtils} from 'models/model-utils';
import {InstanceObject} from 'models/instance-object';
import {RDF_TYPE} from 'instance/instance-properties';
import _ from 'lodash';

const serviceUrl = '/instances';

@Injectable()
@Inject(RestClient)
export class BpmService {

  constructor(restClient) {
    this.restClient = restClient;
    this.config = {};
    this.config.headers = {
      'Accept': HEADER_V2_JSON,
      'Content-Type': HEADER_V2_JSON
    };
  }

  loadModel(id, operation, config) {
    config = _.defaults(config || {}, this.config);
    config.params = {
      operation
    };
    return this.restClient.get(`${serviceUrl}/${id}/model/bpm`, config);
  }

  executeTransition(id, actionPayload, config) {
    config = _.defaults(config || {}, this.config);
    config.skipInterceptor = true;
    return this.restClient.post(`${serviceUrl}/${id}/actions/bpmTransition`, actionPayload, config);
  }

  startBpm(id, actionPayload, config) {
    config = _.defaults(config || {}, this.config);
    return this.restClient.post(`${serviceUrl}/${id}/actions/bpmStart`, actionPayload, config);
  }

  stopBpm(id, actionPayload, config) {
    config = _.defaults(config || {}, this.config);
    return this.restClient.post(`${serviceUrl}/${id}/actions/bpmStop`, actionPayload, config);
  }

  claimBpm(id, actionPayload, config) {
    config = _.defaults(config || {}, this.config);
    return this.restClient.post(`${serviceUrl}/${id}/actions/bpmClaim`, actionPayload, config);
  }

  releaseBpm(id, actionPayload, config) {
    config = _.defaults(config || {}, this.config);
    return this.restClient.post(`${serviceUrl}/${id}/actions/bpmRelease`, actionPayload, config);
  }

  getInfo(id, config) {
    config = _.defaults(config || {}, this.config);
    return this.restClient.get(`${serviceUrl}/${id}/bpm/activity`, config);
  }

  /**
   * Return the process engine for current user.
   *
   * @param config special config to be applied for this rest.
   *
   * @return promise that contains the current engine that the custom rest will use.
   */
  getEngine(config) {
    config = _.defaults(config || {}, this.config);
    return this.restClient.get('/camunda/api/engine', config);
  }

  /**
   * Executes a custom get request to a specifically generated url.
   *
   * @param url the rest end point that the rest.
   *
   * @param config config that is going to be used by the rest client.
   *
   * @return promise with the response of the execution of the rest. NOTE: Since this executes custom url request its response format is not guaranteed !!!
   */
  executeCustomProcessRequestGet(url, config) {
    return this.restClient.get(url, config);
  }

  /**
   * Generates URL request for getting XML data for workflows.
   *
   * @param enginePath the path to the engine.
   *
   * @param definition the definition value that is needed by the rest.
   *
   * @return the path to get workflow definition XML.
   */
  generateXmlURL(enginePath, definition) {
    return `${enginePath}/process-definition/${definition}/xml`;
  }

  /**
   * Generates URL request for getting XML data for workflows.
   *
   * @param enginePath the path to the engine.
   *
   * @param definition the definition value that is needed by the rest.
   *
   * @return the path to get workflow definition XML.
   */
  generateKeyXmlURL(enginePath, definition) {
    return `${enginePath}/process-definition/key/${definition}/xml`;
  }

  /**
   * Generates URL request for getting the process instance.
   *
   * @param enginePath the path to the engine.
   *
   * @param instanceId the id of the instance we want to retrieve.
   *
   * @return the request path to get the process Instance.
   */
  generateProcessInstanceURL(enginePath, instanceId) {
    return `${enginePath}/process-instance/${instanceId}`;
  }

  /**
   * Generates URL request for getting the historical process instance.
   *
   * @param enginePath the path to the engine.
   *
   * @param instanceId the id of the instance we want to retrieve.
   *
   * @return the request path to get the process Instance.
   */
  generateProcessInstanceHistoryURL(enginePath, instanceId) {
    return enginePath + '/history/process-instance/' + instanceId;
  }

  /**
   * Generates URL request for getting current activity progress.
   *
   * @param enginePath the path to the engine.
   *
   * @param instanceId the id of the workflow instance.
   *
   * @return the path to get the current workflow progress.
   */
  generateActivityURL(enginePath, instanceId) {
    return `${enginePath}/process-instance/${instanceId}/activity-instances`;
  }

  /**
   * Generates historical version requests.
   *
   * @param enginePath the path to the engine
   *
   * @param id the id of the process definition with version.
   *
   * @return the path to get a historical version of the bpmn.
   */
  generateVersionXmlUrl(enginePath, id) {
    return `${enginePath}/process-definition/${id}/xml`;
  }

  buildBPMActionPayload(currentObjectId, actionDefinition, currentObjects, operation) {
    let payload = {
      operation,
      userOperation: actionDefinition.action,
      targetInstances: [],
      currentInstance: currentObjectId
    };
    let service = this;
    Object.keys(currentObjects).forEach((i) => {
      let props = service.getInstanceData(currentObjects[i]);
      let temp = {
        instanceId: currentObjects[i].id,
        definitionId: currentObjects[i].getModels().definitionId,
        properties: props
      };
      payload.targetInstances.push(temp);
    });
    return payload;
  }

  /**
   * Collect all instance properties: changed and unchanged one for the BPM payload. The BPM engine expects properties
   * which was initially injected in the instance to be returned back to the engine although they might not been changed.
   *
   * TODO: Discuss eventual change in the backend to not require all properties but only the changeset. Then
   * create an instance from the change-set, then re-apply the injections.
   *
   * @param instance the instance object we want ot process.
   * @return {{}} the data of the instance.
   */
  getInstanceData(instance) {
    let instanceData = {};
    if (!instance.models.validationModel) {
      return instanceData;
    }
    let flatViewModelMap = ModelUtils.flatViewModel(instance.models.viewModel);
    Object.keys(instance.models.validationModel.serialize()).forEach((propertyName) => {
      // 'rdf:type' is escaped because of a problem when saving the instances.
      if (propertyName !== RDF_TYPE) {
        let propertyViewModel = flatViewModelMap.get(propertyName);
        let propertyValidationModel = instance.models.validationModel[propertyName];
        let propertyValues = BpmService.getPropertyValue(propertyViewModel, propertyValidationModel);
        let isEmptyArray = propertyValues.value instanceof Array && propertyValues.value.length === 0;
        if (!isEmptyArray) {
          instanceData[propertyName] = propertyValues.value;
        }
      }
    });
    return instanceData;
  }

  /**
   * This is peace of code is implemented here as the transition action workflow is requires different behavior than in
   * the normal instance saving where only the change-set is sent to backend. In contrast, here it is needed all properties
   * to be returned back to backend - either changed and unchanged one.
   *
   * @return {{defaultValue: *, value: *}}
   */
  static getPropertyValue(propertyViewModel, propertyValidationModel) {
    let defaultValue = propertyValidationModel.defaultValue;
    let value = propertyValidationModel.value;

    if (InstanceObject.isObjectProperty(propertyViewModel)) {
      defaultValue = BpmService.formatObjectPropertyValue(defaultValue);
      value = BpmService.formatObjectPropertyValue(value);
    } else if (InstanceObject.isNumericProperty(propertyViewModel) && value) {
      defaultValue = parseFloat(defaultValue);
      value = parseFloat(value);
    }

    return {
      defaultValue,
      value
    };
  }

  static formatObjectPropertyValue(rawValue) {
    if (rawValue) {
      let hasAddedRelation = (rawValue.add && rawValue.add.length);
      let hasRemovedRelation = (rawValue.remove && rawValue.remove.length);
      // if the property value has been changed by the user, the change set is returned untouched
      if (hasAddedRelation || hasRemovedRelation) {
        return {
          add: rawValue.add,
          remove: rawValue.remove
        };
      }
      // if the property has value which is not changed by the user, a new change-set is built with the default value in
      // place
      if (rawValue.results && rawValue.results.length) {
        return {
          add: rawValue.results,
          remove: []
        };
      }
    }

    // If default value is undefined but user opens the picker and does not select an object then defaultValue will be
    // undefined and value will be {} and property will be considered changed.
    // Or the property value was initially empty, then return an empty change-set.
    return {
      add: [],
      remove: []
    };
  }
}