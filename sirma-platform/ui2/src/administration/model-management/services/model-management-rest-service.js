import {Inject, Injectable} from 'app/app';
import {RestClient} from 'services/rest-client';

const SERVICE_URL = '/administration/model-management';

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
    return this.restClient.get(`${SERVICE_URL}/hierarchy`).then(response => response.data);
  }

  getModelsMetaData() {
    return this.restClient.get(`${SERVICE_URL}/meta-info`).then(response => response.data);
  }

  getModelProperties() {
    return this.restClient.get(`${SERVICE_URL}/properties`).then(response => response.data);
  }

  getModelData(identifier) {
    let encodedId = encodeURIComponent(identifier);
    return this.restClient.get(`${SERVICE_URL}?model=${encodedId}`).then(response => response.data);
  }
}