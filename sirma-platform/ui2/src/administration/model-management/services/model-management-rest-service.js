import {Inject, Injectable} from 'app/app';
import {RestClient} from 'services/rest-client';

export const SERVICE_URL = '/administration/model-management';

/**
 * Rest service providing administrative and management access to models.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(RestClient)
export class ModelManagementRestService {

  constructor(restClient) {
    this.restClient = restClient;
  }

  getModelsHierarchy() {
    return this.restClient.get(`${SERVICE_URL}/hierarchy`).then(response => this.getResponseData(response));
  }

  getModelsMetaData() {
    return this.restClient.get(`${SERVICE_URL}/meta-info`).then(response => this.getResponseData(response));
  }

  getModelProperties() {
    return this.restClient.get(`${SERVICE_URL}/properties`).then(response => this.getResponseData(response));
  }

  getModelData(identifier) {
    let encodedId = encodeURIComponent(identifier);
    return this.restClient.get(`${SERVICE_URL}?model=${encodedId}`).then(response => this.getResponseData(response));
  }

  getModelsForDeploy() {
    return this.restClient.get(`${SERVICE_URL}/deploy`).then(response => this.getResponseData(response));
  }

  saveModelData(data) {
    return this.restClient.post(`${SERVICE_URL}`, data).then(response => this.getResponseData(response));
  }

  deployModels(modelsToDeploy, version) {
    return this.restClient.post(`${SERVICE_URL}/deploy`, {modelsToDeploy, version}).then(response => this.getResponseData(response));
  }

  getResponseData(response) {
    return response.data;
  }
}