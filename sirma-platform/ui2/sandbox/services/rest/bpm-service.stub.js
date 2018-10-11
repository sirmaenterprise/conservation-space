import {Inject, Injectable} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import _ from 'lodash';
import data from 'sandbox/services/rest/bpm-service.data.json!';

@Injectable()
@Inject(PromiseAdapter)
export class BpmService {

  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
    this.config = {};
  }

  loadModel(id, operation, config) {
    config = _.cloneDeep(config || {}, this.config);
    config.params = {
      operation
    };
    return this.promiseAdapter.promise((resolve) => {
      //TODO : add data
      return undefined;
    });
  }

  executeTransition(id, actionPayload, config) {
    config = _.cloneDeep(config || {}, this.config);
    return this.promiseAdapter.promise((resolve) => {
      //TODO : add data
      return undefined;
    });
  }

  startBpm(id, actionPayload, config) {
    config = _.cloneDeep(config || {}, this.config);
    return this.promiseAdapter.promise((resolve) => {
      //TODO : add data
      return undefined;
    });
  }

  getInfo(id, config) {
    config = _.cloneDeep(config || {}, this.config);
    return this.promiseAdapter.promise((resolve) => {
      return resolve(data.infoResponse);
    });
  }

  getEngine(id, config) {
    config = _.cloneDeep(config || {}, this.config);
    return this.promiseAdapter.promise((resolve) => {
       return resolve({data : "thomas"});
    });
  }

  executeCustomProcessRequestGet(url, config) {
      return this.promiseAdapter.promise((resolve) => {
        //We use the custom url to retrieve the corresponding response.
        return resolve(data.customURLResponces[url]);
      });
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
    return enginePath + '/process-definition/' + definition + '/xml';
  }

  /**
   *
   * @param enginePath the path to the engine.
   *
   * @param instanceId the id of the instance we want to retrieve.
   *
   * @return {string} the request path to get the process Instance.
   */
  generateProcessInstanceURL(enginePath, instanceId) {
    return enginePath + '/process-instance/' + instanceId;
  }

  /**
   * Generates URL request for getting the process instance.
   *
   * @param enginePath the path to the engine.
   *
   * @param instanceId the id of the instance we want to retrieve.
   *
   * @return {string} the request path to get the process Instance.
   */
  generateProcessInstanceHistoryURL(enginePath, instanceId) {
    return enginePath + '/history/process-instance/' + instanceId;
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
}