import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';
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
   * @return promise that contains the current engine that the cusstom rest will use.
   */
  getEngine(config) {
    config = _.defaults(config || {}, this.config);
    return this.restClient.get('/camunda/api/engine', config);
  }

  /**
   * Executes a custom get reguest to a specificly generated url.
   *
   * @param url the rest end point that the rest.
   *
   * @param config config that is going to be used by the rest client.
   *
   * @return promise with the responce of the execution of the rest. NOTE: Since this executes custom url request its response format is not guaranteed !!!
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
    return enginePath + '/process-definition/key/' + definition + '/xml';
  }

  /**
   * Generates URL request for getting cuurent activity progress.
   *
   * @param enginePath the path to the engine.
   *
   * @param instanceId the id of the workflow instance.
   *
   * @return the path to get the current workflow progress.
   */
  generateActivityURL(enginePath, instanceId) {
    return enginePath + '/process-instance/' + instanceId + '/activity-instances';
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
    return enginePath +'/process-definition/' + id + '/xml';
  }

  buildBPMActionPayload(currentObjectId, actionDefinition, currentObjects, operation) {
    let payload = {
      operation: operation,
      userOperation: actionDefinition.action,
      targetInstances: [],
      currentInstance: currentObjectId
    };

    Object.keys(currentObjects).forEach((i) => {
      let props = currentObjects[i].getChangeset();
      let temp = {
        instanceId: currentObjects[i].id,
        definitionId: currentObjects[i].getModels().definitionId,
        properties: props
      };
      payload.targetInstances.push(temp);
    });
    return payload;
  }
}