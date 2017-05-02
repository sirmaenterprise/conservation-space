import {Inject, Injectable} from 'app/app';
import {ACCEPT, CONTENT_TYPE} from 'services/rest/http-headers';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';

export const SERVICE_BASE_URL = '/rolemgmt';

@Injectable()
@Inject(RestClient)
export class RoleManagementService {

  constructor(restClient) {
    this.restClient = restClient;
    this.config = {};
    this.config.headers = {};
    this.config.headers[ACCEPT] = HEADER_V2_JSON;
    this.config.headers[CONTENT_TYPE] = HEADER_V2_JSON;
  }

  /**
   * Fetches all the roles in the system.
   */
  getRoles() {
    return this.restClient.get(SERVICE_BASE_URL + '/roles', this.config);
  }

  /**
   * Fetches all the actions in the system.
   */
  getActions() {
    return this.restClient.get(SERVICE_BASE_URL + '/actions', this.config);
  }

  /**
   * Fetches the mappings between role and action.
   */
  getRoleActions() {
    return this.restClient.get(SERVICE_BASE_URL + '/roleActions', this.config);
  }

  /**
   * Fetches all the filters in the system, that are allowed to be added to a role and action mapping.
   */
  getFilters() {
    return this.restClient.get(SERVICE_BASE_URL + '/filters', this.config);
  }

  /**
   * Sends save request for the given role actions.
   *
   * @param roleActions the role actions must be in the following format:
   * [{
   *    "action": "actionId",
   *    "role": "roleId",
   *    "enabled": true/false,
   *    "filters": ["CREATEDBY", etc]
   * }, {
   *  ...
   * }]
   * @returns {*} all the role actions
   */
  saveRoleActions(roleActions) {
    return this.restClient.post(SERVICE_BASE_URL + '/roleActions', roleActions, this.config);
  }

}