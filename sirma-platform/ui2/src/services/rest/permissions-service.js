import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';
import _ from 'lodash';

export const SERVICE_URL = '/instances';

@Injectable()
@Inject(RestClient)
export class PermissionsRestService {

  constructor(restClient) {
    this.restClient = restClient;
    this.config = {};
    this.config.headers = {
      'Accept': HEADER_V2_JSON,
      'Content-Type': HEADER_V2_JSON
    };
  }

  /**
   * Load permissions for an instance.
   *
   * @param instanceId
   *            Instance id.
   *  @param includeParentPermissions
   *            Include parent permissions in the response.
   */
  load(instanceId, includeParentPermissions, includeLibraryPermissions) {
    let requestConfig = _.defaults({
      params: {
        includeInherited: includeParentPermissions,
        includeLibrary: includeLibraryPermissions
      }
    }, this.config);
    let uri = SERVICE_URL + '/' + instanceId + '/permissions';
    return this.restClient.get(uri, requestConfig);
  }

  /**
   * Saves the permissions to a given instance.
   * @param instanceId the id of the instance
   * @param permissions a valid json object in the following format
   * {"permissions": [{
   *  "id": "user identifier",
   *  "special": "CONSUMER"
   * },
   * {
   *  "id": "other user identifier",
   *  "special": "COLLABORATOR"
   * }],
   * "inheritedPermissionsEnabled": true}
   */
  save(instanceId, permissions) {
    return this.restClient.post(SERVICE_URL + '/' + instanceId + '/permissions', permissions, this.config);
  }

  /**
   * Calls the backend to restore the permissions from the parent.
   *
   * @param instanceId
   *            the target instance id.
   * @return JSON containing the number for the scheduled entries
   */
  restoreChildrenPermissions(instanceId) {
    let uri = SERVICE_URL + '/' + instanceId + '/permissions/restore-from-parent';
    return this.restClient.post(uri, this.config);
  }

  /**
   * Getter for the roles in our system.
   *
   * @returns the roles
   */
  getRoles() {
    return this.restClient.get(SERVICE_URL + '/permissions/roles');
  }
}